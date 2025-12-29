FROM node:18-alpine as build-stage
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .

# Build Arguments
ARG PORT_FRONTEND_MENU
ARG PORT_GATEWAY
ENV PORT_FRONTEND_MENU=$PORT_FRONTEND_MENU
ENV PORT_GATEWAY=$PORT_GATEWAY

RUN npm run build

FROM nginx:stable-alpine as production-stage
COPY --from=build-stage /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
