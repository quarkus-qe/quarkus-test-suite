<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://maven.apache.org/POM/4.0.0"
                xmlns:pom="http://maven.apache.org/POM/4.0.0"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <xsl:output method="xml" encoding="UTF-8" omit-xml-declaration="no"/>
    <!-- Copy everything -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    <!-- Remove explicit "enabled" tag from all platform members -->
    <xsl:template match="/pom:project/pom:build/pom:pluginManagement/pom:plugins/pom:plugin[./pom:artifactId/text() = 'quarkus-platform-bom-maven-plugin']/pom:configuration/pom:platformConfig/pom:members/pom:member/pom:enabled"/>
    <!-- Disable all platform members, but langchain4j and MCP Server -->
    <xsl:template match="/pom:project/pom:build/pom:pluginManagement/pom:plugins/pom:plugin[./pom:artifactId/text() = 'quarkus-platform-bom-maven-plugin']/pom:configuration/pom:platformConfig/pom:members/pom:member[pom:name/text()!='LangChain4j' and pom:name/text()!='MCPServer']">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()" />
            <enabled>false</enabled>
            <xsl:text>&#xA;</xsl:text>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>