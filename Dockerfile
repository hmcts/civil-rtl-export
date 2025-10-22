 # renovate: datasource=github-releases depName=microsoft/ApplicationInsights-Java
ARG APP_INSIGHTS_AGENT_VERSION=3.7.5
FROM hmctspublic.azurecr.io/base/java:21-distroless
USER hmcts

COPY lib/applicationinsights.json /opt/app/
COPY build/libs/civil-rtl-export.jar /opt/app/

EXPOSE 4550
CMD [ "civil-rtl-export.jar" ]
