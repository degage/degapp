package utility;

import java.lang.reflect.Field;

/**
 * Created by Cedric on 3/7/14.
 */
public class Cloner {

    // Quick and dirty method for cloning objects (without using serialization)
    // Source: http://www.stupidjavatricks.com/2007/09/cloning-an-object-using-reflection/
    public static Object clone(Object o)
    {
        Object clone = null;

        try
        {
            clone = o.getClass().newInstance();
        }
        catch (InstantiationException e)
        {
            e.printStackTrace();
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }

        // Walk up the superclass hierarchy
        for (Class obj = o.getClass();
             !obj.equals(Object.class);
             obj = obj.getSuperclass())
        {
            Field[] fields = obj.getDeclaredFields();
            for (int i = 0; i < fields.length; i++)
            {
                fields[i].setAccessible(true);
                try
                {
                    // for each class/suerclass, copy all fields
                    // from this object to the clone
                    fields[i].set(clone, fields[i].get(o));
                }
                catch (IllegalArgumentException e){}
                catch (IllegalAccessException e){}
            }
        }
        return clone;
    }
}
