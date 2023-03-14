package com.qlive.uikitcore;

import java.util.List;

public class UIJsonConfOption {

    /**
     * appid : xxx
     * config : [{"name":"beauty","flag":0},{"name":"ui","flag":1},{"name":"relay","flag":0},{"name":"mic","flag":0},{"name":"item","flag":0},{"name":"gift","flag":0},{"name":"bulletScreen","flag":0},{"name":"booking","flag":0},{"name":"like","flag":0},{"name":"announcement","flag":0}]
     */

    private String appid;
    private List<ConfigBean> config;

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public List<ConfigBean> getConfig() {
        return config;
    }

    public void setConfig(List<ConfigBean> config) {
        this.config = config;
    }

    public static class ConfigBean {
        /**
         * name : beauty
         * flag : 0
         */

        private String name;
        private int flag;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getFlag() {
            return flag;
        }

        public void setFlag(int flag) {
            this.flag = flag;
        }
    }
}
