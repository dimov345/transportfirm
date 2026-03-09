# ---- Build stage ----
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci --ignore-scripts
COPY . .
RUN npm run build:prod

# ---- Runtime stage ----
FROM node:20-alpine
WORKDIR /app
COPY --from=build /app/dist/transportfirm_FE/server ./server
COPY --from=build /app/dist/transportfirm_FE/browser ./browser
EXPOSE 4000
ENV PORT=4000
CMD ["node", "server/server.mjs"]
