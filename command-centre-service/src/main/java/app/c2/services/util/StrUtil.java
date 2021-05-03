package app.c2.services.util;

public class StrUtil {
    public static boolean isEmpty(String str){
        if(str == null || str.trim().length()==0){
            return true;
        }else{
            return false;
        }
    }
}
