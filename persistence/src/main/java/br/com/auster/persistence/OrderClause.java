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

/**
 * Defines a ordering criteria for query operations.
 *  <P> 
 * @author framos
 * @version $Id$
 */
public class OrderClause implements Serializable {

	
	
    // ----------------------
    // Instance variables
    // ----------------------
	
	private String fieldName;
	private boolean ascending;	
	
	
	
    // ----------------------
    // Constructors
    // ----------------------

	/**
	 * Creates a new order clause using the specified field name and setting it
	 * 	to be ascending.
	 * 
	 * @param _property the name of the field by which order the query
	 */
	public OrderClause(String _property) {
	    this(_property, true);
	}

	/**
	 * Creates a new order clause using the specified field name, and setting it 
	 * 	to be ascending if the boolena parameter is set to <code>true</code>.
	 * 
	 * @param _property the name of the field by which order the query
	 * @param _asc if the order is ascending or descending
	 */
	public OrderClause(String _property, boolean _asc) {
	    this.fieldName = _property;
	    this.ascending = _asc;
	}

	
	
    // ----------------------
    // Public variables
    // ----------------------
	
	public final void setAscending(boolean _asc) {
	    this.ascending = _asc;
	}
	
	public final boolean isAscending() {
	    return this.ascending;
	}
	
	public final String getFieldName() {
	    return this.fieldName;
    }
	        
}
