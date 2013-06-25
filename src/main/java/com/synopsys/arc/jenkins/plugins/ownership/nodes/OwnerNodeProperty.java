package com.synopsys.arc.jenkins.plugins.ownership.nodes;

import com.synopsys.arc.jenkins.plugins.ownership.IOwnershipHelper;
import com.synopsys.arc.jenkins.plugins.ownership.IOwnershipItem;
import com.synopsys.arc.jenkins.plugins.ownership.OwnershipDescription;
import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Node;
import hudson.slaves.NodeProperty;
import hudson.slaves.NodePropertyDescriptor;
import hudson.slaves.SlaveComputer;
import java.util.List;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.Ancestor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Implements owner property for Jenkins Nodes
 * 
 * @author Oleg Nenashev <nenashev@synopsys.com>
 * @since 0.0.3
 */
public class OwnerNodeProperty extends NodeProperty<Node> 
    implements IOwnershipItem<NodeProperty> {

    OwnershipDescription ownership;
    String nodeName; 
    
    @DataBoundConstructor
    public OwnerNodeProperty(Node node, OwnershipDescription ownership) {
         setNode(node);
         this.nodeName = node.getNodeName();
         this.ownership = ownership;
    }
    
    public OwnershipDescription getOwnership()
    {             
        return ownership;
    }

    public Node getNode() {
        if (node == null) {
            setNode(Jenkins.getInstance().getNode(nodeName));
        }
        return node;
    }

    @Override
    public NodePropertyDescriptor getDescriptor() {
        return super.getDescriptor(); //To change body of generated methods, choose Tools | Templates.
    }
            
    @Override
    public IOwnershipHelper<NodeProperty> helper() {
        
        return NodeOwnerPropertyHelper.Instance;
    }

    @Override
    public NodeProperty<?> reconfigure(StaplerRequest req, JSONObject form) throws Descriptor.FormException {
        return super.reconfigure(req, form); //To change body of generated methods, choose Tools | Templates.
    }
     
    @Override
    public NodeProperty getDescribedItem() {
        return this;
    }
      
    @Extension
    public static class DescriptorImpl extends NodePropertyDescriptor {
        
        @Override
        public NodeProperty<?> newInstance( StaplerRequest req, JSONObject formData ) throws Descriptor.FormException {               
            Node owner = getNodePropertyOwner(req);
            OwnershipDescription descr = OwnershipDescription.Parse(formData, "slaveOwnership");
            return descr.isOwnershipEnabled() ? new OwnerNodeProperty(owner, descr) : null;       
        }
         
        /**
         * Gets Node, which is being configured by StaplerRequest
         * @remarks Workaround for
         * @param req StaplerRequest (ex, from NodePropertyDescriptor::newInstance())
         * @return Instance of the node, which is being configured (or null)
         * 
         * @since 0.0.5
         * @author Oleg Nenashev <nenashev@synopsys.com>
         */
        private static Node getNodePropertyOwner(StaplerRequest req)
        {                
             List<Ancestor> ancestors = req.getAncestors();
            if (ancestors.isEmpty())
            {
                assert false : "StaplerRequest is empty";
                return null;
            }
            
            Object node = ancestors.get(ancestors.size()-1).getObject();
            if (SlaveComputer.class.isAssignableFrom(node.getClass()))
            {
                return ((SlaveComputer)node).getNode();
            }
            else
            {
                assert false : "StaplerRequest should have Node ancestor";
                return null;
            }
        }
        
        @Override
        public String getDisplayName() {
                return "Slave Owner";
        }

        @Override
        public boolean isApplicable( Class<? extends Node> Type ) {
                return true;
        }           
    }

    @Override
    protected final void setNode(Node node) {
        super.setNode(node); //To change body of generated methods, choose Tools | Templates.
    } 
}