package ru.netvoxlab.ownradio;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by a.polunina on 09.12.2016.
 */

public class TrackModel {
	@SerializedName("id")
	@Expose
	private String id;
	@SerializedName("name")
	@Expose
	private String name;
	@SerializedName("artist")
	@Expose
	private String artist;
	@SerializedName("length")
	@Expose
	private String length;
	@SerializedName("methodid")
	@Expose
	private String methodid;

	/**
	 * No args constructor for use in serialization
	 *
	 */
	public TrackModel() {
	}

	/**
	 *
	 * @param id
	 * @param methodid
	 * @param name
	 * @param length
	 * @param artist
	 */
	public TrackModel(String id, String name, String artist, String length, String methodid) {
		super();
		this.id = id;
		this.name = name;
		this.artist = artist;
		this.length = length;
		this.methodid = methodid;
	}

	/**
	 *
	 * @return
	 * The id
	 */
	public String getId() {
		return id;
	}

	/**
	 *
	 * @param id
	 * The id
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 *
	 * @return
	 * The name
	 */
	public String getName() {
		return name;
	}

	/**
	 *
	 * @param name
	 * The name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 *
	 * @return
	 * The artist
	 */
	public String getArtist() {
		return artist;
	}

	/**
	 *
	 * @param artist
	 * The artist
	 */
	public void setArtist(String artist) {
		this.artist = artist;
	}

	/**
	 *
	 * @return
	 * The length
	 */
	public String getLength() {
		return length;
	}

	/**
	 *
	 * @param length
	 * The length
	 */
	public void setLength(String length) {
		this.length = length;
	}

	/**
	 *
	 * @return
	 * The methodid
	 */
	public String getMethodid() {
		return methodid;
	}

	/**
	 *
	 * @param methodid
	 * The methodid
	 */
	public void setMethodid(String methodid) {
		this.methodid = methodid;
	}
}
