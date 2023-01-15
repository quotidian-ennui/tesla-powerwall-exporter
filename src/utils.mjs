import winston from 'winston';
import {
  Site, Battery, Load, Solar, powerwallStateOfCharge, SystemStatus,
} from './metrics.mjs';

const { combine, timestamp, json } = winston.format;

const logger = winston.createLogger({
  level: process.env.LOG_LEVEL || 'info',
  format: combine(timestamp(), json()),
  transports: [
    new winston.transports.Console(),
  ],
});

const waitFor = (millSeconds) => new Promise((resolve) => {
  setTimeout(() => {
    resolve();
  }, millSeconds);
});

const retryPromiseWithExpotentialDelay = async (promise, retries) => {
  try {
    const res = await promise;
    return res;
  } catch (error) {
    if (retries < 1) {
      return Promise.reject(error);
    }
    logger.info(`retrying promise, sleeping for ${2 ** retries}`);
    await waitFor((2 ** retries) * 1000);
    return retryPromiseWithExpotentialDelay(promise, retries - 1);
  }
};

/*
 * Query all aggregated metrics
 */
const updateMetrics = async (tesla) => {
  logger.verbose('Scraping powerwall');
  const metrics = await retryPromiseWithExpotentialDelay(tesla.aggregates(), 3);

  [Site, Battery, Load, Solar].forEach((category) => {
    // eslint-disable-next-line array-callback-return,consistent-return
    Object.keys(category).filter((e) => { if (e !== 'toString') return e; }).forEach((metric) => {
      category[metric].set(metrics[category.toString().toLowerCase()][metric]);
    });
  });

  const soe = await retryPromiseWithExpotentialDelay(tesla.soe(), 3);
  powerwallStateOfCharge.set(soe.percentage);
  const systemStatus = await retryPromiseWithExpotentialDelay(tesla.systemStatus(), 3);

  Object.keys(SystemStatus).forEach((metric) => {
    SystemStatus[metric].set(systemStatus[metric]);
  });

  logger.verbose('Finished scraping powerwall');
};

function listenPort() {
  const envPort = parseInt(process.env.PORT, 10) || 0;
  const envNodePort = parseInt(process.env.NODE_PORT, 10) || 0;
  if (envNodePort === 0) {
    return envPort === 0 ? 9961 : envPort;
  }
  return envNodePort;
}

export {
  updateMetrics,
  logger,
  listenPort,
};
