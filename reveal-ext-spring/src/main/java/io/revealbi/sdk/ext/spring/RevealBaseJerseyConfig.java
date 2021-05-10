package io.revealbi.sdk.ext.spring;

import org.glassfish.jersey.server.ResourceConfig;

import com.infragistics.reveal.engine.init.InitializeParameter;
import com.infragistics.reveal.engine.init.RevealEngineInitializer;

public abstract class RevealBaseJerseyConfig extends ResourceConfig {
    public RevealBaseJerseyConfig() {
        RevealEngineInitializer.initialize(getRevealInitializeParameter());
        
        for (Class<?> clazz : RevealEngineInitializer.getClassesToRegister()) {
        	register(clazz);
        }
    }
    
    protected abstract InitializeParameter getRevealInitializeParameter();
}