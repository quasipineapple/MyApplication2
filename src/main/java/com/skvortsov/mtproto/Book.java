
package com.skvortsov.mtproto;

import java.util.List;

public class Book{
   	private List<Constructor> constructors;
   	private List<Method> methods;

 	public List<Constructor> getConstructors(){
		return this.constructors;
	}
	public void setConstructors(List<Constructor> constructors){
		this.constructors = constructors;
	}
 	public List<Method> getMethods(){
		return this.methods;
	}
	public void setMethods(List<Method> methods){
		this.methods = methods;
	}
    public Constructor getConstructorById(String id){

        for (Constructor element : constructors) {
            if (element.getId().equals(id)) {
                return element;
            }
        }

        throw new RuntimeException("Cannot find Constructor with id: " + id);

    }
    public Constructor getConstructorByPredicate(String predicate){
        for (Constructor element : constructors) {
            if (element.getPredicate().equals(predicate)) {
                return element;
            }
        }
        throw new RuntimeException("Cannot find Constructor with predicate: " + predicate);
    }

    public Method getMethodByName(String s) {
        for(Method m : methods){
            if(m.getMethod().equals(s)){
                return m;
            }
        }
        throw new RuntimeException("Cannot find Method with name: " + s);
    }
}
