FROM node:19.3.0-alpine3.16 AS stage1
COPY src/ /app/src
COPY ["package.json", "package-lock.json", "server.mjs", "/app/"]
WORKDIR /app
RUN npm install --only=production

FROM node:19.3.0-alpine3.16
COPY --from=stage1 /app /app
WORKDIR /app
CMD ["npm", "run", "start"]
