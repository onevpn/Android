package co.onevpn.android.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class User{
    private List<Server> servers;
    private Map<String, String> message;
    private Map<String, String> auth;
    private Map<String, String> bandwidth;
    private PayPlan plan;
    private Map<String, String> geo;
    private Map<String, String> ipsec;
    private Map<String, String> pay;
    private String email;
    private String password;

    public List<Server> getServers() {
        return new ArrayList<>(servers);
    }

    public Map<String, String> getMessage() {
        return message;
    }

    public Map<String, String> getBandwidth() {
        return bandwidth;
    }

    public PayPlan getPlan() {
        return plan;
    }

    public Map<String, String> getGeo() {
        return geo;
    }

    public Map<String, String> getIpsec() {
        return ipsec;
    }

    public Map<String, String> getAuth() {
        return auth;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Map<String, String> getPay() {
        return pay;
    }

    public void setServers(List<Server> servers) {
        this.servers = servers;
    }

    public void setBandwidth(Map<String, String> bandwidth) {
        this.bandwidth = bandwidth;
    }

    public static class PayPlan {
        @SerializedName("plan_expired")
        private int planExpired;
        @SerializedName("plan_name")
        private String planName;
        @SerializedName("days_paid")
        private int daysPaid;

        public int getPlanExpired() {
            return planExpired;
        }



        public String getPlanName() {
            return planName;
        }

        public int getDaysPaid() {
            return daysPaid;
        }

        public boolean isTrial() {
            return "Trial".equals(planName);
        }

        public void setPlanExpired(int planExpired) {
            this.planExpired = planExpired;
        }
    }

    public static class Server {
        @SerializedName("Name")
        private String name;
        @SerializedName("Flag")
        private String flag;
        @SerializedName("Country")
        private String country;
        @SerializedName("DNS")
        private String dns;
        private int pingTime;
        private boolean isFavorite;
        private boolean isSelected;

        private List<String> protocol;

        public String getName() {
            return name;
        }

        public String getCountry() {
            return country;
        }

        public String getDns() {
            return dns;
        }

        public List<String> getProtocol() {
            return protocol;
        }

        public int getPingTime() {
            return pingTime;
        }

        public void setPingTime(int pingTime) {
            this.pingTime = pingTime;
        }

        public boolean isFavorite() {
            return isFavorite;
        }

        public void setFavorite(boolean favorite) {
            isFavorite = favorite;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof Server)) {
                return false;
            }

            return getDns().equals( ( (Server) obj).getDns() );
        }

        @Override
        public int hashCode() {
            return name.hashCode() + 3 * country.hashCode() + 11 * dns.hashCode();
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(boolean selected) {
            isSelected = selected;
        }

        public String getFlag() {
            return flag;
        }
    }
}
