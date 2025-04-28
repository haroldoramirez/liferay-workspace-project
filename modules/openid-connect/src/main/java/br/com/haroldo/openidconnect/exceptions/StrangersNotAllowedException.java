package br.com.haroldo.openidconnect.exceptions;

import com.liferay.portal.kernel.exception.PortalException;

public class StrangersNotAllowedException extends PortalException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public StrangersNotAllowedException(long companyId) {
		super(String.format("Company %s does not allow strangers", companyId));

		this.companyId = companyId;
	}

	public final long companyId;

}