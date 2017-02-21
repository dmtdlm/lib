package ru.dmtdlm.dummy;

/**
 * Created by dmtdlm on 21.02.2017.
 */
public class ConditionParser
{
    private int i;
    private String s;
    private int max;

    private interface ConditionInterface
    {
        public void print();
    }

    private class ConditionTxt implements ConditionInterface
    {

        @Override
        public void print()
        {

        }
    }

    private class PredicateTxt implements ConditionInterface
    {

        @Override
        public void print()
        {

        }
    }

    private String parseSub(int level)
    {
        boolean isRight = false;
        String left = "";
        String right = "";
        String current = "";
        String operation = "";

        boolean started = false;

        while (i <= max)
        {
            char c = s.charAt(i);
            char cNext = s.charAt(i + 1);
            String doubleSymb = "" + c + cNext;
            if (c == '(')
            {
                i++;
                current = parseSub(level + 1);
            }
            else if (c == ')')
                break;
            else if (doubleSymb.equals("&&") || doubleSymb.equals("||"))
            {
                i++;
                isRight = true;
                left = current;
                current = "";
                started = false;
                operation = doubleSymb;
            }
            else if (c != ' ' || started)
            {
                current += c;
                started = true;
            }

            i++;
        }

        if (isRight)
            right = current;
        else
            left = current;

        System.out.print(level + ": ");
        System.out.print(left + " - ");
        System.out.print(operation + " - ");
        System.out.println(right);

        return left + operation + right;

    }

    public void parse(String s)
    {
        i = 0;
        this.s = s + " ";
        this.max = s.length() - 1;

        parseSub(0);
    }
}
