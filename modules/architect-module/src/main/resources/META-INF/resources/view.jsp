<%@ include file="/init.jsp" %>

<portlet:actionURL name="check_tls" var="checkTlsURL" />

<p>
	<b><liferay-ui:message key="architectmodule.caption"/></b>
</p>

<form action="${checkTlsURL}" method="post">
    <button type="submit">Verificar versão TLS</button>
</form>

<%
    String protocols = ParamUtil.getString(renderRequest, "protocols");
	String enabledProtocols = ParamUtil.getString(renderRequest, "enabledProtocols");

    if (protocols != null && !protocols.isEmpty() && enabledProtocols != null && !enabledProtocols.isEmpty()) {
%>
    <div style="margin-top: 20px;">
        <strong>Protocolos disponiveis:</strong> <%= protocols %></br>
        <strong>Protocolos ativos:</strong> <%= enabledProtocols %>
    </div>
<%
    }
%>