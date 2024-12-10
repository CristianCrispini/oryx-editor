# oryx-editor
Automatically exported from code.google.com/p/oryx-editor

2015-03-26 Switch to GitHub and Restart of Development

The basis of this repository is the oryx-editor project as hosted on google-code 
before. As the project has been abondoned some years ago (due to the makers going 
commercial) and Google-Code is going to be discontinued, we imported the 
code-base (check tag: "google-code-version") to GitHub and will attempt to further 
develop the project here.

What is it? Oryx is a platform for conceptual modelling (UML, EPC, BPMN, etc.) that
provides access to the stored models by URLs. It's fully functional in terms of
creating and editing models and can easily be extended, e.g. by other notations
(called stencil-sets) or by extra functionality for certain notations (via plugins).

Note:
You need to perform the following steps to get a running system:
* cp ./build.properties.example to ./build.properties and adjust it to your environment
* cp ./poem-jvm/etc/hibernate.cfg.xml.example to ./poem-jvm/etc/hibernate.cfg.xml and adjust it to your environment

Current Roadmap
* Focus on bringing the code up-to-date (adapt to changes in newer versions of Postgres and alike)
* Provide local login as an alternative to using OpenID
* Add stencil-set for entity relationship models (ERM)
* Extend stencil-set for event-driven process-chains (EPC) with additional elements
* Fixing bugs

TOMCAT8!
* brew install tomcat@8
  /opt/homebrew/etc/tomcat
  /opt/homebrew/etc/tomcat/Catalina
  /opt/homebrew/etc/tomcat/Catalina/localhost
  /opt/homebrew/etc/tomcat/catalina.policy
  /opt/homebrew/etc/tomcat/catalina.properties
  /opt/homebrew/etc/tomcat/context.xml
  /opt/homebrew/etc/tomcat/jaspic-providers.xml
  /opt/homebrew/etc/tomcat/jaspic-providers.xsd
  /opt/homebrew/etc/tomcat/logging.properties
  /opt/homebrew/etc/tomcat/server.xml
  /opt/homebrew/etc/tomcat/tomcat-users.xml
  /opt/homebrew/etc/tomcat/tomcat-users.xsd
  /opt/homebrew/etc/tomcat/web.xml

  docker run --name poem-plpython  -p 5432:5432 -e POSTGRES_HOST_AUTH_METHOD=trust \
    -v pgdata:/var/lib/postgresql/data \
    postgres-python2