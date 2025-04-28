Projeto para Integração de Login OpenID Connect para o Liferay
==============================

Esses módulos representam uma possível integração ao RHSSO do Banco no Liferay.

O RHSSO parece transmitir o nome de uma pessoa como um nome completo, enquanto a implementação OpenID Connect da Liferay transmite o nome em
dois campos separados esperados. Este módulo, portanto, divide o nome em nome e sobrenome. Além disso, foi prestado apoio à
Adoção da data de nascimento adicionada.

Instalação
------------

Após a implantação dos módulos, os arquivos armazenados no módulo `verimi-openid-connect-service-handler` devem ser copiados para `liferay.home/osgi/configs`:

* `com.liferay.portal.security.sso.openid.connect.internal.OpenIdConnectServiceHandlerImpl.config`

Aplicativo
---------

Verimi é um provedor OpenID Connect. Para configurar isso, um novo provedor OpenID Connect deve ser configurado no Liferay. Aqueles necessários para isso
Os dados devem ser fornecidos pela Verimi.

Novos provedores OpenID Connect são armazenados em Painel de Controle > Configuração > Configurações do Sistema > Segurança > SSO. Em geral, o OpenID Connect é usado aqui
ativado no item *OpenID Connect*. Uma nova entrada para Verimi pode ser armazenada em *Provider OpenID Connect*.

Este módulo suporta as seguintes entradas de escopo:
    
    nome openid e-mail data de nascimento

Versão do Liferay
-----------------

Este plugin foi testado com sucesso na seguinte versão do Liferay:

* Liferay CE 7.2.0 GA1

Fontes
-----------------------

Hintergrundinformationen zur Entwicklung können diesem Blog-Post entnommen werden: https://liferay.dev/blogs/-/blogs/integrating-verimi-with-liferay
Github - https://github.com/xdotgmbh/verimi-liferay-integration
OSGi (Open Service Gateway Initiative) - https://www.osgi.org/resources/where-to-start/
Liferay Module Projects - https://learn.liferay.com/w/dxp/liferay-development/liferay-internals/fundamentals/module-projects
