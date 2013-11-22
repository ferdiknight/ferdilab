/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.blueferdi.diamondbox.arrays;

/**
 *
 * @author ferdinand
 */
public class Arrangement {

    private static String tmp;
    
    public void swap(String[] strs,int src,int dist)
    {
        tmp = strs[src];
        strs[src] = strs[dist];
        strs[dist] = tmp;
    }
            
    
    public void arrange(String[] strs,int start,int end) {
        
        if(end == start)
        {
            for(int i=0;i<=end;i++)
                System.out.print(strs[i]);
            
            System.out.println();
        }
        else
        {
            for(int i=start;i<=end;i++)
            {
                swap(strs,start,i);
                arrangeAllInOne(strs,start+1,end);
                swap(strs,start,i);
            }
        }
        
    }
    
    public void arrangeAllInOne(String[] strs,int start,int end) {
        
        
        if(end == start)
        {
            for(int i=0;i<=end;i++)
                System.out.print(strs[i]);
            
            System.out.println();
        }
        else
        {
            String tmpinner;
            for(int i=start;i<=end;i++)
            {
                tmpinner = strs[start];
                strs[start]=strs[i];
                strs[i] = tmpinner;
                
                arrange(strs,start+1,end);
                
                tmpinner = strs[start];
                strs[start]=strs[i];
                strs[i] = tmpinner;
            }
        }
        
    }
    
    public static void main(String[] args) {
//        Arrangement a = new Arrangement();
//        a.arrangeAllInOne(new String[]{"a","b","c","d","e"}, 0, 4);
        
        System.out.println(80<100?90:100.0);
        
    }
}
