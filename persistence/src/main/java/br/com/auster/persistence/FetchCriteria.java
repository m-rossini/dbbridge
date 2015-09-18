/*
 * Copyright (c) 2004 Auster Solutions. All Rights Reserved.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Created on Mai 10, 2005
 */
package br.com.auster.persistence;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class allows for query operations to be defined limits for fetching the results. You can define a starting point, named 
 * 	offset, a maximum size of records and a list of odering criterias, made out of instances of <code>OrderClause</code>.
 *  <P> 
 * @author framos
 * @version $Id$
 */
public class FetchCriteria implements Serializable {

	
	
    // ----------------------
    // Instance variables
    // ----------------------
	
    private int offset;
    private int size;
    private List orders;
    
    
    
    // ----------------------
    // Constructors
    // ----------------------
    
    public FetchCriteria() {
        orders = new ArrayList();
    }
    
    

    // ----------------------
    // Public methods
    // ----------------------
	
    public final int getOffset() {
        return offset;
    }
    public final void setOffset(int offset) {
        this.offset = offset;
    }
    
    public final int getSize() {
        return size;
    }

    public final void setSize(int size) {
        this.size = size;
    }
    
    public final void addOrder(String _field, boolean _asc) {
        addOrder(_field, _asc, 0);
    }

    public final void addOrder(String _field, boolean _asc, int _pos) {
        orders.add(_pos, new OrderClause(_field, _asc));
    }

    public final void clearOrder() {
        orders.clear();
    }
    
    public final Iterator orderIterator() {
        return orders.iterator();
    }
}
