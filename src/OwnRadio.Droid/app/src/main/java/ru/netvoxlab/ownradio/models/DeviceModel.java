package ru.netvoxlab.ownradio.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by a.polunina on 07.06.2017.
 */

public class DeviceModel {
	@SerializedName("recid")
	@Expose
	private String recId;
	@SerializedName("recname")
	@Expose
	private String recName;
	
	public DeviceModel() {
		super();
	}
	
	/**
	 *
	 * @param recId
	 * @param recName
	 */
	public DeviceModel(String recId, String recName) {
		super();
		this.recId = recId;
		this.recName = recName;
	}
	/**
	 *
	 * @return
	 * The recId
	 */
	public String getRecId() { return recId; }
	
	/**
	 *
	 * @return
	 * The recName
	 */
	public String getRecName() {
		return recName;
	}
	
	/**
	 *
	 * @param recId
	 * The recId
	 */
	public void setRecId(String recId) { this.recId = recId; }
	
	/**
	 *
	 * @param recName
	 * The lastListen
	 */
	public void setRecName(String recName) {
		this.recName = recName;
	}
}
