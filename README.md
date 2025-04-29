# Integração de Login OpenID Connect para o Liferay

Este projeto foi implementado para estender a autenticação de usuários via screenName, realizar o redirecionamento para a tela do Login Provider caso o usuário não esteja autenticado e, também, para processar/importar novos usuários utilizando o screenName como critério, reimplementando a lógica contida nos pacotes Internal do Liferay.

## Implantação dos módulos

Primeiramente devemos realizar o deployment do pacote `openid-connect-exposer-fragment`  e na sequência o pacote `openid-connect`.

## Instalação

Após a implantação dos módulos, o arquivo de configuração armazenado no módulo openid-connect deve ser copiado para a pasta `configs` dentro do servidor Liferay, conforme exemplo: `liferay.home.servidor/osgi/configs`:


```config
com.liferay.portal.security.sso.openid.connect.internal.OpenIdConnectServiceHandlerImpl.config
```

## Configuração do Openid Connect Provider no Liferay

Devemos configurar um novo provedor. As informações devem ser fornecidas pelo mantenedor do RHSSO.

Novos provedores OpenID Connect são mantidos no Painel de Controle > Configuração > Configurações do Sistema > Segurança > Login único (SSO) > OpenID Connect.

Após Habilitar a configuração Openid Connect, podemos adicionar um novo *Provider OpenID Connect*.

Este módulo suporta as seguintes entradas de escopo:

```config
openid email profile
```

## Versão do Liferay

Este módulo foi testado com sucesso na seguinte versão do Liferay:

- Liferay CE 7.2.0 GA1

## Ferramentas utilizadas

Liferay Developer Studio 3.8.1.202004240132-ga2,
Maven 3.6.3, Openjdk 8u442-b06

## Referências

 - [Liferay Source Code - 7.2.0 GA1](https://github.com/liferay/liferay-portal/releases/tag/7.2.0-ga1)
 - [Liferay Dev Blog - verimi-liferay-integration](https://liferay.dev/blogs/-/blogs/integrating-verimi-with-liferay)
 - [OSGi (Open Service Gateway Initiative)](https://www.osgi.org/resources/where-to-start/)
 - [Liferay Module Projects](https://learn.liferay.com/w/dxp/liferay-development/liferay-internals/fundamentals/module-projects)
