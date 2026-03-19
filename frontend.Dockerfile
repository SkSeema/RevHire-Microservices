FROM nginx:1.27-alpine

ARG API_URL=http://localhost:9090/api

RUN printf "server {\n    listen 80;\n    server_name _;\n\n    root /usr/share/nginx/html;\n    index index.html;\n\n    location / {\n        try_files \$uri \$uri/ /index.html;\n    }\n}\n" > /etc/nginx/conf.d/default.conf
COPY RevHire-HiringPlatform--Frontend/dist/revhire-frontend/browser /usr/share/nginx/html
RUN find /usr/share/nginx/html -name '*.js' -exec sed -i "s|http://3.109.55.112:9090/api|${API_URL}|g" {} +

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
