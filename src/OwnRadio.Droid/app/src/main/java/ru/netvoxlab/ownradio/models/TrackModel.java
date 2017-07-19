package ru.netvoxlab.ownradio.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by a.polunina on 13.07.2017.
 */

public class TrackModel {
	@SerializedName("recId")
	@Expose
	private String recId;
	@SerializedName("recName")
	@Expose
	private String recName;
	@SerializedName("artist")
	@Expose
	private String artist;
	@SerializedName("length")
	@Expose
	private Integer length;
	@SerializedName("size")
	@Expose
	private Integer size;
	@SerializedName("iscorrect")
	@Expose
	private Integer iscorrect;
	
	public String getRecId() {
		return recId;
	}
	
	public void setRecId(String recId) {
		this.recId = recId;
	}
	
	public String getRecName() {
		return recName;
	}
	
	public void setRecName(String recName) {
		this.recName = recName;
	}
	
	public String getArtist() {
		return artist;
	}
	
	public void setArtist(String artist) {
		this.artist = artist;
	}
	
	public Integer getLength() {
		return length;
	}
	
	public void setLength(Integer length) {
		this.length = length;
	}
	
	public Integer getSize() {
		return size;
	}
	
	public void setSize(Integer size) {
		this.size = size;
	}
	
	public Integer getIscorrect() {
		return iscorrect;
	}
	
	public void setIscorrect(Integer iscorrect) {
		this.iscorrect = iscorrect;
	}
}
