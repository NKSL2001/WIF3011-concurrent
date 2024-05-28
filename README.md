## Getting Started

Welcome to the VS Code Java world. Here is a guideline to help you get started to write Java code in Visual Studio Code.

## Folder Structure

The workspace contains two folders by default, where:

- `src`: the folder to maintain sources
- `lib`: the folder to maintain dependencies

Meanwhile, the compiled output files will be generated in the `bin` folder by default.

> If you want to customize the folder structure, open `.vscode/settings.json` and update the related settings there.

## Web server

The project requires a graphical interface showing the graph comparison between different devices and file sizes. To achieve the target, we use Apache Tomcat server.

### Set up web server

1. Install [Community Server Connectors](https://marketplace.visualstudio.com/items?itemName=redhat.vscode-community-server-connector) and restart VS Code.
2. Find Servers panel at the bottom of Explorer side panel.
3. Right click on Community Server Connectors and choose "Create New Server..."
4. Choose "Yes" to download a server, choose "Apache Tomcat 11.0.0-M6". Select "Continue" to agree to the license.
5. Wait for download to finish.
6. In the Servers panel, right click on apache-tomcat-11.0.0-M6 and choose "Add Deployment".
7. Select "Exploded" to load the folder instead of using a compiled WAR file.
8. Choose the root folder of this project. (The folder should directly contains the subfolder `WEB-INF`)
9. Click "Select Exploded Deployment", then choose "No" for optional deployment parameters.

### Publish site to web server

1. Make sure the compiled Java `.class` files are saved in `WEB-INF/classes`.
2. Right click on apache-tomcat-11.0.0-M6 server.
3. Choose "Publish Server (Full)".
4. Right click on apache-tomcat-11.0.0-M6 server.
5. Choose "Start Server".

### Access webpages

1. Open your browser and visit [`http://localhost:8080`](http://localhost:8080). You should see the Tomcat page.
2. Visit `http://localhost:8080/[YOUR PROJECT ROOT FOLDER NAME]`, such as [`http://localhost:8080/WIF3011-concurrent`](http://localhost:8080/WIF3011-concurrent) if directly cloned from this repo. You should see the "Hello World Project" page.
3. Visit `http://localhost:8080/[YOUR PROJECT ROOT FOLDER NAME]/servlet`, such as [`http://localhost:8080/WIF3011-concurrent/servlet`](http://localhost:8080/WIF3011-concurrent) if directly cloned from this repo. You should see the "Hello World Servlet" page.


## Dependency Management

The `JAVA PROJECTS` view allows you to manage your dependencies. More details can be found [here](https://github.com/microsoft/vscode-java-dependency#manage-dependencies).
