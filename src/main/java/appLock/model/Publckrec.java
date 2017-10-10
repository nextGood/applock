package appLock.model;

public class Publckrec {
	private String reckey;

	private String lcktim;

	private String timout;

	private String updflg;

	public String getReckey() {
		return reckey;
	}

	public void setReckey(String reckey) {
		this.reckey = reckey == null ? null : reckey.trim();
	}

	public String getLcktim() {
		return lcktim;
	}

	public void setLcktim(String lcktim) {
		this.lcktim = lcktim == null ? null : lcktim.trim();
	}

	public String getTimout() {
		return timout;
	}

	public void setTimout(String timout) {
		this.timout = timout == null ? null : timout.trim();
	}

	public String getUpdflg() {
		return updflg;
	}

	public void setUpdflg(String updflg) {
		this.updflg = updflg == null ? null : updflg.trim();
	}
}