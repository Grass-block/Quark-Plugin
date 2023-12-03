package org.tbstcraft.quark.util.container;

import java.util.HashMap;

/**
 * simple start argument
 *
 * @author GrassBlock2022
 */
public class StartArguments {
    public final HashMap<String,String> dispatchedArgs=new HashMap<>();

    /**
     * feed arg to object
     * @param args args(you know,main(String[] args))
     */
    public StartArguments(String[] args){
        for (String arg:args){
            StringBuilder sb=new StringBuilder();
            String k=null;
            String v;
            for (char c:arg.toCharArray()){
                if(c=='='){
                    k=sb.toString();
                    sb=new StringBuilder();
                }else{
                    sb.append(c);
                }
            }
            v=sb.toString();
            dispatchedArgs.put(k,v);
        }
    }

    /**
     * get value as int
     * @param id id
     * @param fallback fallback value
     * @return value,if it can not find than use fallback.
     */
    public int getValueAsInt(String id, int fallback) {
        return Integer.parseInt(dispatchedArgs.getOrDefault(id, String.valueOf(fallback)));
    }

    /**
     * get value as boolean
     * @param id id
     * @param fallback fallback value
     * @return value,if it can not find than use fallback.
     */
    public boolean getValueAsBoolean(String id,boolean fallback){
        return Boolean.parseBoolean(dispatchedArgs.getOrDefault(id, String.valueOf(fallback)));
    }

    /**
     * get value as String
     * @param id id
     * @param fallback fallback value
     * @return value,if it can not find than use fallback.
     */
    public String getValueAsString(String id, String fallback) {
        return dispatchedArgs.getOrDefault(id,fallback);
    }
}
