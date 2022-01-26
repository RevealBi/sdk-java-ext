package io.revealbi.sdk.ext.rest;

/**
 * Factory for the user context provider used by REST services (data sources, dashboards, etc).
 * By default it uses an instance of {@link DefaultRestUserContextProvider} and can be changed using {@link #setInstance(IRestUserContextProvider)} method.
 */
public class RestUserContextProviderFactory {
	private static IRestUserContextProvider instance = new DefaultRestUserContextProvider();

	/**
	 * Changes the current user context provider to the specified instance
	 * @param instance Required object implementing {@link IRestUserContextProvider}.
	 * @throws NullPointerException if the parameter is {@code null}.
	 */
	public static void setInstance(IRestUserContextProvider instance) throws NullPointerException {
		if (instance == null) {
			throw new NullPointerException();
		}
		RestUserContextProviderFactory.instance = instance;
	}
	
	/**
	 * Gets the current instance of the user context provider.
	 * @return the current user context provider.
	 */
	public static IRestUserContextProvider getInstance() {
		return instance;
	}
}
