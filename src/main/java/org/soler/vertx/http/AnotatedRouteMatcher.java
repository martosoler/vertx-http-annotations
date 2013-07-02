package org.soler.vertx.http;

import io.netty.handler.codec.http.HttpMethod;

import java.lang.reflect.Method;
import java.util.Set;

import org.reflections.Reflections;
import org.soler.vertx.http.annotation.RequestMatcher;
import org.soler.vertx.http.annotation.VertxRouteMatcher;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;


public class AnotatedRouteMatcher extends RouteMatcher {

	public AnotatedRouteMatcher(String prefix) {
		super();
		
		inspectMatchers(prefix);
	}

	private void inspectMatchers(String prefix) {
		Reflections r = new Reflections(prefix);
		
		Set<Class<?>> classes = r.getTypesAnnotatedWith(VertxRouteMatcher.class);;
		
		for (final Class<?> clazz : classes) {
			Method[] methods = clazz.getMethods();

			for (final Method m : methods) {
				if (m.isAnnotationPresent(RequestMatcher.class)) {
					System.out.println(m.getName());
					
					RequestMatcher requestMatcherAnonotation = m.getAnnotation(RequestMatcher.class);
					
					String requestMapping = requestMatcherAnonotation.value();
					String httpMethod = requestMatcherAnonotation.httpMethod();

					AnnotatedHandler handler = new AnnotatedHandler(m, clazz);

					// TODO: Replace this if chain with polymorphism
					if (HttpMethod.GET.name().equalsIgnoreCase(httpMethod)) {
						this.get(requestMapping, handler);
					}

					if (HttpMethod.POST.name().equalsIgnoreCase(httpMethod)) {
						this.post(requestMapping, handler);
					}
					
				}
			}
		}
	}

	private class AnnotatedHandler implements Handler<HttpServerRequest> {

		private Method method;
		private Class<?> clazz;

		public AnnotatedHandler(Method m, Class<?> c) {
			this.method = m;
			this.clazz = c;
		}

		public void handle(HttpServerRequest request) {
			try {
				this.method.invoke(clazz.newInstance(), request);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		
		
	}

}
