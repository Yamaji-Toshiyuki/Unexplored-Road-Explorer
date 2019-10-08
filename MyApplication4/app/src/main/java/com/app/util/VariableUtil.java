package com.app.util;

import java.io.Serializable;

public class VariableUtil implements Serializable {

	public static final String SERIAL_NAME = "VariableUtil";

	private boolean isLocationService;
	private boolean isForeground;
	private boolean isApplicationStop;

	//////////////
	// セッター
	//////////////
	public void setIsLocationService(boolean bol){
		isLocationService = bol;
	}

	public void setIsForeground(boolean bol){
		isForeground = bol;
	}

	public void setIsApplicationStop(boolean bol){
		isApplicationStop = bol;
	}

	//////////////
	// ゲッター
	//////////////
	public boolean getIsLocationService(){
		return isLocationService;
	}

	public boolean getIsForeground(){
		return isForeground;
	}

	public boolean getIsApplicationStop(){
		return isApplicationStop;
	}
}
