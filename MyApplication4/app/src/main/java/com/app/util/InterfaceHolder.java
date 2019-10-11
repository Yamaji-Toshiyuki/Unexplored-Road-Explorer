package com.app.util;

public class InterfaceHolder {

	private static OpenDialog openDialog;

	public static void set(OpenDialog openDialog) {
		InterfaceHolder.openDialog = openDialog;
	}

	public static OpenDialog get() {
		return openDialog;
	}
}
