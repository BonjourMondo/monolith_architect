import java.util.*;

/**
 * Author: leesanghyuk
 * Date: 2020-02-14 10:30
 * Description:
 */
class Solution {
    public List<Integer> topKFrequent(int[] nums, int k) {
        if(nums.length==0||k==0) return null;
        List<Integer> ans=new ArrayList<>();
        TreeMap<Integer,Integer> map=new TreeMap<>();//降序
        for (int i:nums){
            map.put(i,map.getOrDefault(i,0)+1);
        }

        map.forEach((key,value)->{
            System.out.println(key+" "+value);
            ans.add(key);
        });
        return ans.subList(0,k);
    }
}
