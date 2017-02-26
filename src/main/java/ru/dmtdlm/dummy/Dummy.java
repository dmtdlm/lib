package ru.dmtdlm.dummy;

import ru.dmtdlm.lib.stringcondition.StringCondition;

import java.io.File;
import java.io.IOException;

/**
 * Created by Dmitry on 19.02.2017.
 */
public class Dummy
{
    public static void main(String[] args) throws IOException
    {
        //String s = "((($a==b)||!(b==$u)&&f=1 || !($u==$2 || h=8)&&l=9)&&a==b)||h==0";
        //String s = "(a == $b) && ($c == $d || $e == f) && !GG";
        //StringCondition stringCondition = StringCondition.parseCondition(s);

        StringCondition stringCondition = StringCondition.parseFileOr(new File("c.txt"));

        stringCondition.setVariable("b","a");
        stringCondition.setVariable("c","3323");
        stringCondition.setVariable("d","33323");
        stringCondition.setVariable("e","f");

        //stringCondition.print();
        //stringCondition.setFlag("GG",true);

        //stringCondition.print();


        //System.out.println(stringCondition.calculate());


        stringCondition.print();

        /*
        s = "aaa ==   bbb ";
        Pattern pattern = Pattern.compile("\\s*(\\S+)\\s*(==|!=)\\s*(\\S+)\\s*");
        Matcher matcher = pattern.matcher(s);
        System.out.println(matcher.groupCount());
        System.out.println(matcher.matches());
        System.out.println(matcher.group(1));
        System.out.println(matcher.group(2));
        System.out.println(matcher.group(3));
        */

        /*
        s = "as\\\\\\\\\\=abc";
        System.out.println(s.replaceAll("\\\\(.)","$1"));
        System.out.println(s);
        */

        //s = "dsd   sd   sd      g";
        //System.out.println(s.replaceAll("\\s*$",""));
    }
}

