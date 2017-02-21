package ru.dmtdlm.dummy;

/**
 * Created by Dmitry on 19.02.2017.
 */
public class Dummy
{
    public static void main(String[] args)
    {
        String s = "((a==b)&&(b==c)||(f=1))";
        ConditionParser conditionParser = new ConditionParser();

        conditionParser.parse(s);
    }
}

