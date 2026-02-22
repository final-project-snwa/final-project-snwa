#FROM nginx:1.28.1-alpine
#
#RUN rm /etc/nginx/conf.d/default.conf
#COPY ./default.conf /etc/nginx/conf.d/default.conf
#
#EXPOSE 80
#CMD ["nginx", "-g", "daemon off;"]


FROM nginx:1.28.1-alpine

RUN rm /etc/nginx/conf.d/default.conf
COPY conf/default.conf /etc/nginx/conf.d/default.conf
COPY conf/service-url.inc /etc/nginx/conf.d/service-url.inc

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]