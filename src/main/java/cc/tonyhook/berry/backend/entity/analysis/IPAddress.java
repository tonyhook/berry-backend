package cc.tonyhook.berry.backend.entity.analysis;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "analysis_ipaddress")
public class IPAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String version;

    private String IPAddressStart;

    private String IPAddressEnd;

    private String cat;

    private String regionCode;

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getIPAddressStart() {
        return this.IPAddressStart;
    }

    public void setIPAddressStart(String IPAddressStart) {
        this.IPAddressStart = IPAddressStart;
    }

    public String getIPAddressEnd() {
        return this.IPAddressEnd;
    }

    public void setIPAddressEnd(String IPAddressEnd) {
        this.IPAddressEnd = IPAddressEnd;
    }

    public String getCat() {
        return this.cat;
    }

    public void setCat(String cat) {
        this.cat = cat;
    }

    public String getRegionCode() {
        return this.regionCode;
    }

    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }

}
