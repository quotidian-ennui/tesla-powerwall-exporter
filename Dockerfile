FROM node:20.10.0-alpine AS stage1
COPY src/ /app/src
COPY ["package.json", "package-lock.json", "server.mjs", "/app/"]
WORKDIR /app
RUN npm install --only=production

FROM node:20.10.0-alpine
COPY --from=stage1 /app /app
WORKDIR /app
CMD ["npm", "run", "start"]
