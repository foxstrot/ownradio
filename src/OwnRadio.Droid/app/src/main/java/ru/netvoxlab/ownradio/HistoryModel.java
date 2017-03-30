package ru.netvoxlab.ownradio;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by a.polunina on 11.12.2016.
 */

public class HistoryModel {
	@SerializedName("lastListen")
	@Expose
	private String lastListen;
	@SerializedName("isListen")
	@Expose
	private int isListen;
//	@SerializedName("methodid")
//	@Expose
//	private int methodid;

	public HistoryModel() {
		super();
	}

	/**
	 *
	 * @param lastListen
//	 * @param methodid
	 * @param isListen
	 */
	public HistoryModel(String lastListen, int isListen/*, int methodid*/) {
		super();
		this.lastListen = lastListen;
		this.isListen = isListen;
//		this.methodid = methodid;
	}
	/**
	 *
	 * @return
	 * The lastListen
	 */
	public String getLastListen() {
		return lastListen;
	}

	/**
	 *
	 * @param lastListen
	 * The lastListen
	 */
	public void setLastListen(String lastListen) {
		this.lastListen = lastListen;
	}

	/**
	 *
	 * @return
	 * The isListen
	 */
	public int getIsListen() {
		return isListen;
	}

	/**
	 *
	 * @param isListen
	 * The isListen
	 */
	public void setIsListen(int isListen) {
		this.isListen = isListen;
	}

	/**
	 *
	 * @return
	 * The methodid
	 */
//	public int getMethodid() {
//		return methodid;
//	}

	/**
	 *
	 * @param methodid
	 * The methodid
	 */
//	public void setMethodid(int methodid) {
//		this.methodid = methodid;
//	}

}
