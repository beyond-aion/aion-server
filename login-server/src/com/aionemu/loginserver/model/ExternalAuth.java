package com.aionemu.loginserver.model;


/**
 * Holds authentication information for use with external authentication.
 * 
 * @author Neon
 */
public class ExternalAuth {

	/**
	 * account identifier
	 */
	private String id;

	/**
	 * account authentication state
	 */
	private Integer state;

	/**
	 * returns account identifier to work with in LS DB (account_data.ext_auth_name)
	 * 
	 * @return identifier<br>
	 *          null if not set
	 */
	public String getIdentifier() {
		return id;
	}

	/**
	 * sets account identifier to work with in LS DB (account_data.ext_auth_name)
	 * 
	 * @param identifier
	 */
	public void setIdentifier(String identifier) {
		this.id = identifier;
	}

	/**
	 * @return authentication state (see {@link com.aionemu.loginserver.network.aion.AionAuthResponse})<br>
	 *          null if not set
	 */
	public Integer getAuthState() {
		return state;
	}

	/**
	 * Sets account authentication state (see {@link com.aionemu.loginserver.network.aion.AionAuthResponse})
	 * 
	 * @param state
	 */
	public void setAuthState(Integer state) {
		this.state = state;
	}

	/**
	 * Returns true if identifiers match
	 * 
	 * @param o
	 *          another auth to check
	 * @return true if account identifiers match
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (!(o instanceof ExternalAuth)) {
			return false;
		}

		ExternalAuth auth = (ExternalAuth) o;

		if ((id != null) ? !id.equals(auth.id) : auth.id != null) {
			return false;
		}

		return true;
	}

	/**
	 * Returns auth hash code (based on account identifier).
	 * 
	 * @return auth hash code
	 */
	@Override
	public int hashCode() {
		return (id != null) ? id.hashCode() : 0;
	}
}
