package cc.tonyhook.berry.backend.entity.visitor;

import java.util.Base64;

public class ProfileBitmap {

    private Boolean finished;

    private Integer timelen;

    private Integer pv;

    private Boolean[] bits;

    public Boolean isFinished() {
        return this.finished;
    }

    public Boolean getFinished() {
        return this.finished;
    }

    public void setFinished(Boolean finished) {
        this.finished = finished;
    }

    public Integer getTimelen() {
        return this.timelen;
    }

    public void setTimelen(Integer timelen) {
        this.timelen = timelen;
    }

    public Integer getPv() {
        return this.pv;
    }

    public void setPv(Integer pv) {
        this.pv = pv;
    }

    public Boolean[] getBits() {
        return this.bits;
    }

    public void setBits(Boolean[] bits) {
        this.bits = bits;
    }

    public Double getPercentage() {
        if (this.bits == null) {
            return 0.0;
        }

        Integer counter = 0;
        for (int i = 0; i < 128; i++) {
            if (this.bits[i]) {
                counter++;
            }
        }
        return counter / 128.0;
    }

    public Boolean isTailed(Double tail) {
        if (this.bits == null) {
            return false;
        }

        Integer counter = 0;
        for (int i = (int) Math.round(128 * tail); i < 128; i++) {
            if (this.bits[i]) {
                counter++;
            }
        }
        return counter > 0;
    }

    public Boolean isFullyWatched(Double tail, Double percentage) {
        return this.isTailed(tail) && this.getPercentage() > percentage;
    }

    @Override
    public String toString() {
        String str = "";
        Integer tvalue = 0;

        if (finished) {
            str += "Y";
        } else {
            str += "N";
        }

        tvalue = pv;
        if (tvalue < 0) {
            tvalue = 0;
        }
        if (tvalue > 9999) {
            tvalue = 9999;
        }
        str += String.format("%04d", tvalue);

        tvalue = timelen;
        if (tvalue < 0) {
            tvalue = 0;
        }
        if (tvalue > 9999) {
            tvalue = 9999;
        }
        str += String.format("%04d", tvalue);

        if (bits != null) {
            byte b = 0;
            byte[] src = new byte[16];

            for (int i = 0; i < 128; i++) {
                if (bits[i]) {
                    b = (byte)(b * 2 + 1);
                } else {
                    b = (byte)(b * 2 + 0);
                }

                if (i % 8 == 7) {
                    src[i / 8] = b;
                    b = 0;
                }
            }

            str += Base64.getEncoder().encodeToString(src);
        }

        return str;
    }

    public static ProfileBitmap fromString(String str) {
        ProfileBitmap bitmap = new ProfileBitmap();
        byte[] src = null;
        Boolean[] bits = new Boolean[128];

        if (str.length() == 9) {
            bitmap.setFinished(str.substring(0, 1).equals("Y"));
            bitmap.setPv(Integer.parseInt(str.substring(1, 5)));
            bitmap.setTimelen(Integer.parseInt(str.substring(5, 9)));
            bitmap.setBits(null);

            return bitmap;
        } else if (str.length() == 33) {
            bitmap.setFinished(str.substring(0, 1).equals("Y"));
            bitmap.setPv(Integer.parseInt(str.substring(1, 5)));
            bitmap.setTimelen(Integer.parseInt(str.substring(5, 9)));
            src = Base64.getDecoder().decode(str.substring(9, 33).getBytes());

            for (int i = 0; i < 16; i++) {
                byte b = src[i];
                for (int j = 0; j < 8; j++) {
                    bits[i * 8 + j] = (b < 0);
                    b = (byte)(b << 1);
                }
            }

            bitmap.setBits(bits);

            return bitmap;
        } else {
            bitmap.setFinished(false);
            bitmap.setPv(0);
            bitmap.setTimelen(0);
            bitmap.setBits(null);

            return bitmap;
        }
    }

    public void merge(ProfileBitmap bitmap) {
        if (bitmap.getFinished()) {
            this.setFinished(true);
        }

        this.setPv(this.getPv() + bitmap.getPv());
        this.setTimelen(this.getTimelen() + bitmap.getTimelen());

        if (bitmap.getBits() != null) {
            if (this.getBits() == null) {
                this.setBits(bitmap.getBits());
            } else {
                Boolean[] mergedBits = new Boolean[128];
                for (int i = 0; i < 128; i++) {
                    mergedBits[i] = this.getBits()[i] || bitmap.getBits()[i];
                }
                this.setBits(mergedBits);
            }
        }
    }

}
