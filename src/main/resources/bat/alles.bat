@echo off

java -jar AAATools-${project.version}.jar -c "Konfigurationen/AAAKatalog-GV-1.0.0-SCXML.xml" 
java -jar AAATools-${project.version}.jar -c "Konfigurationen/AAAKatalog-LB-1.0.1-SCXML.xml" 
java -jar AAATools-${project.version}.jar -c "Konfigurationen/AAAKatalog-LB-1.0.1-Aenderungen-SCXML.xml"
java -jar AAATools-${project.version}.jar -c "Konfigurationen/AAAKatalog-LN-1.0.2-SCXML.xml" 
java -jar AAATools-${project.version}.jar -c "Konfigurationen/AAAKatalog-LN-1.0.2-Aenderungen-SCXML.xml" 
java -jar AAATools-${project.version}.jar -c "Konfigurationen/AAAKatalog-AK-2.0.0-SCXML.xml" 
java -jar AAATools-${project.version}.jar -c "Konfigurationen/AAAKatalog-AS-7.1.2-SCXML.xml" 
java -jar AAATools-${project.version}.jar -c "Konfigurationen/AAAKatalog-AS-7.1.2_ohne_Nutzungsartkennung-SCXML.xml" 
java -jar AAATools-${project.version}.jar -c "Konfigurationen/AAAProfil-DLKM-7.1.2-SCXML.xml" 
java -jar AAATools-${project.version}.jar -c "Konfigurationen/AAAProfil-Basis-DLM-7.1.2-SCXML.xml"
java -jar AAATools-${project.version}.jar -c "Konfigurationen/NAS-GV-1.0.0.xml" 
java -jar AAATools-${project.version}.jar -c "Konfigurationen/NAS-LB-1.0.1.xml" 
java -jar AAATools-${project.version}.jar -c "Konfigurationen/NAS-LN-1.0.2.xml" 
java -jar AAATools-${project.version}.jar -c "Konfigurationen/NAS-AK-2.0.0.xml" 
java -jar AAATools-${project.version}.jar -c "Konfigurationen/NAS-7.1.2.xml"

for /r "Ausgaben" %%F in (katalog?.adoc) do @(
    @echo Generiere HTML aus %%~dpnxF   
    call asciidoctor %%~dpnxF
    @echo Generiere PDF aus %%~dpnxF
    call asciidoctor-pdf -a pdf-themesdir=%%~dpF/resources/themes -a pdf-theme=basic %%~dpnxF
)

PAUSE