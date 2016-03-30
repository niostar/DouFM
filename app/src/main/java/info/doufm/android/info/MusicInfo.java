package info.doufm.android.info;

/**
 * Created by WJ on 2015/1/28.
 */
public class MusicInfo{

    private String key;
    private String title;
    private String artist;
    private String album;
    private String company;
    private String public_time;
    private String kbps;
    private String cover;
    private String audio;
    private String upload_date;
    private boolean isHasCache;
    private boolean isLoved;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getPublic_time() {
        return public_time;
    }

    public void setPublic_time(String public_time) {
        this.public_time = public_time;
    }

    public String getKbps() {
        return kbps;
    }

    public void setKbps(String kbps) {
        this.kbps = kbps;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getAudio() {
        return audio;
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }

    public String getUpload_date() {
        return upload_date;
    }

    public void setUpload_date(String upload_date) {
        this.upload_date = upload_date;
    }

    public boolean isHasCache() {
        return isHasCache;
    }

    public void setHasCache(boolean isHasCache) {
        this.isHasCache = isHasCache;
    }

    public boolean isLoved() {
        return isLoved;
    }

    public void setLoved(boolean isLoved) {
        this.isLoved = isLoved;
    }

    @Override
    public String toString() {
        return "MusicInfo{" +
                "key='" + key + '\'' +
                ", title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", album='" + album + '\'' +
                ", company='" + company + '\'' +
                ", public_time='" + public_time + '\'' +
                ", kbps='" + kbps + '\'' +
                ", cover='" + cover + '\'' +
                ", audio='" + audio + '\'' +
                ", upload_date='" + upload_date + '\'' +
                ", isHasCache=" + isHasCache +
                ", isLoved=" + isLoved +
                '}';
    }
}
