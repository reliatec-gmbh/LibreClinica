package org.akaza.openclinica.domain.admin;

/**
 * Enumeration of types who 2-FA security keys provision is handled within the
 * system.
 * 
 * @author thillger
 */
public enum TwoFactorType {
	/**
	 * The standard way doing how 2-FA security key provision is handled. QR code
	 * will be displayed within the application itself.
	 */
	APPLICATION,
	/**
	 * A much more secure way of providing security key via PDF printout which can
	 * be sent to study sites.
	 */
	LETTER;
}
