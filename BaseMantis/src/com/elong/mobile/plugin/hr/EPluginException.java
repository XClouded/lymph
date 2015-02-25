package com.elong.mobile.plugin.hr;

public class EPluginException extends RuntimeException {
	private static final long serialVersionUID = -678316181407974341L;

	public EPluginException(String msg) {
		super(msg);
	}

	// TODO add other type exception.

	public static class Null extends EPluginException {
		private static final long serialVersionUID = -1335596436403247866L;

		public Null(String msg) {
			super(msg);
		}
	}

	public static class CreateError extends EPluginException {
		private static final long serialVersionUID = -9113716875593673416L;

		public CreateError(String msg) {
			super(msg);
		}

	}
}
