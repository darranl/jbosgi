<?xml version="1.0"?>
<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<!-- 
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 -->
  <xsl:template match="/">
    <HTML>
      <head>
      </head>
      <BODY>
        <h1>Performance Test Report</h1>
        <h2>Service Creation Test</h2>
        This test creates a number of Services and registers these with the OSGi Service Registry. 
        <xsl:call-template name="ChartBenchmark">
          <xsl:with-param name="results" select="/tests/test/result[@type='REG']"/>
        </xsl:call-template>

        <h2>Service Lookup Test</h2>
        This test looks up a number of different services from the OSGi Service Registry and makes an invocation on each.
        <xsl:call-template name="ChartBenchmark">
          <xsl:with-param name="results" select="/tests/test/result[@type='LKU']"/>
        </xsl:call-template>

        <h2>Bundle Installation Test</h2>
        This tests installs and starts bundles with a number of dependencies.
        <xsl:call-template name="ChartBenchmark">
          <xsl:with-param name="results" select="/tests/test/result[@type='IS']"/>
        </xsl:call-template>
      </BODY>
    </HTML>
  </xsl:template>
  
  <xsl:template name="ChartBenchmark">
    <xsl:param name="results"/>
    <xsl:variable name="cb">http://chart.apis.google.com/chart?cht=lxy&amp;chs=500x300&amp;chxt=x,y,x,y&amp;chco=3072F3,ff0000,00aaaa&amp;chls=2,4,1&amp;chm=s,000000,0,-1,5|s,000000,1,-1,5&amp;chdlp=r&amp;</xsl:variable>
    
    <xsl:variable name="yvalues">
      <xsl:call-template name="SortStrList">
        <xsl:with-param name="strlst">
          <xsl:call-template name="GetYMeasurements">
            <xsl:with-param name="params" select="$results"/>
          </xsl:call-template>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
            
    <xsl:variable name="populations">
      <xsl:call-template name="SortStrList">
        <xsl:with-param name="strlst">
          <xsl:call-template name="StrSet">
            <xsl:with-param name="strlst">
              <xsl:call-template name="GetValues">
                <xsl:with-param name="params" select="$results/../parameters/parameter[@name='Population']"/>
              </xsl:call-template>
            </xsl:with-param>
          </xsl:call-template>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="maxy">
      <xsl:call-template name="StrLstLastItem">
        <xsl:with-param name="strlst" select="$yvalues"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="maxx">
      <xsl:call-template name="StrLstLastItem">
        <xsl:with-param name="strlst" select="$populations"/>
      </xsl:call-template>
    </xsl:variable>
    
    <xsl:variable name="xrange" select="concat('0,', $maxx)"/>
    <xsl:variable name="yrange" select="concat('0,', $maxy)"/>

    <xsl:variable name="frameworks">
      <xsl:call-template name="StrSet">
        <xsl:with-param name="strlst">
          <xsl:call-template name="GetValues">
            <xsl:with-param name="params" select="$results/../sysprops/prop[@name='framework']"/>
          </xsl:call-template>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
        
    <xsl:variable name="ranges">
    <xsl:call-template name="RemoveTrailingChar">
      <xsl:with-param name="str">
        <xsl:call-template name="StrListAppendForEach">
          <xsl:with-param name="strlst" select="$frameworks"/>
          <xsl:with-param name="val" select="concat($xrange, ',', $yrange, ',')"/>
        </xsl:call-template>
      </xsl:with-param>
      <xsl:with-param name="char" select="string(',')"/>
    </xsl:call-template>
    </xsl:variable>
    
    <xsl:variable name="x-label" select="$results/@x-axis"/>
    <xsl:variable name="y-label" select="$results/@y-axis"/>
    <xsl:variable name="cb1" select="concat($cb, 'chxr=0,', $xrange, '|1,', $yrange,'&amp;chds=', $ranges)"/>
    <xsl:variable name="cb2" select="concat($cb1, '&amp;chxl=2:||', translate($x-label, ' ', '+'), '||3:||', translate($y-label, ' ', '+'), '|&amp;')"/>

    <xsl:variable name="googlechartmeasurings">
      <xsl:call-template name="GetAllGoogleChartMeasurings">
        <xsl:with-param name="populations" select="$populations" />
        <xsl:with-param name="resultset" select="$results" />
        <xsl:with-param name="frameworks" select="$frameworks" />
      </xsl:call-template>
    </xsl:variable>    
                 
    <xsl:variable name="cb3" select="concat($cb2, 'chdl=', translate($frameworks, ',', '|'), '&amp;', 'chd=t:', $googlechartmeasurings)"/>
    <p/>
    <img>
      <xsl:attribute name="src"><xsl:value-of select="$cb3"/></xsl:attribute>
    </img>
    <p/>
    <b>Chart Data</b><br/>
    Number of threads: <xsl:value-of select="$results/../parameters/parameter[@name='Threads']/@value"/><br/>
    Total Memory Available: <xsl:value-of select="round($results/../runtime/@max-memory div 1024 div 1024)"/> Megabytes<br/>
    Populations: <xsl:value-of select="$populations"/><br/>
    Measurements: <xsl:for-each select="$results">
      x: <xsl:value-of select="@x-value"/> y: <xsl:value-of select="@y-value"/> 
    </xsl:for-each><br/>      
        
    <!--  Sorting Tests -->    
    <!-- 
    <br/>sort list: 40,10,100,5,20
    Sort list:#<xsl:call-template name="SortStrList">
      <xsl:with-param name="strlst">40,10,100,5,20</xsl:with-param>
    </xsl:call-template>
    
    <br/>sort list: 10,100
    Sort list:#<xsl:call-template name="SortStrList">
      <xsl:with-param name="strlst">10,100</xsl:with-param>
    </xsl:call-template>

    <br/>sort list: 10
    Sort list:#<xsl:call-template name="SortStrList">
      <xsl:with-param name="strlst">10</xsl:with-param>
    </xsl:call-template>

    <br/>sort list: 9,8,7,6,5,4,3,2,1
    Sort list:#<xsl:call-template name="SortStrList">
      <xsl:with-param name="strlst">9,8,7,6,5,4,3,2,1</xsl:with-param>
    </xsl:call-template>
    -->
    
    <!-- Sorted Insert Tests -->
    <!-- 
    <br/>sorting input: 5,10,40(20):
    Sorting result:#<xsl:call-template name="InsertStrList">
      <xsl:with-param name="strlst">5,10,40</xsl:with-param>
      <xsl:with-param name="val">20</xsl:with-param>
    </xsl:call-template>    
        
    <br/>sorting input: (20):
    Sorting result:#<xsl:call-template name="InsertStrList">
      <xsl:with-param name="strlst"></xsl:with-param>
      <xsl:with-param name="val">20</xsl:with-param>
    </xsl:call-template>    

    <br/>sorting input: 10(20):
    Sorting result:#<xsl:call-template name="InsertStrList">
      <xsl:with-param name="strlst">10</xsl:with-param>
      <xsl:with-param name="val">20</xsl:with-param>
    </xsl:call-template>    

    <br/>sorting input: 20(10):
    Sorting result:#<xsl:call-template name="InsertStrList">
      <xsl:with-param name="strlst">20</xsl:with-param>
      <xsl:with-param name="val">10</xsl:with-param>
    </xsl:call-template>    
    -->
    
    <!-- StrSet Tests-->
    <!-- 
    <br/>input 10,10,10,100,100,1000:
    Result:#<xsl:call-template name="StrSet">
      <xsl:with-param name="strlst">10,10,10,100,100,1000</xsl:with-param>
    </xsl:call-template>#
     
    <br/>input 10,100:
    Result:#<xsl:call-template name="StrSet">
      <xsl:with-param name="strlst">10,100</xsl:with-param>
    </xsl:call-template>#
    
    <br/>input 100,10:
    Result:#<xsl:call-template name="StrSet">
      <xsl:with-param name="strlst">100,10</xsl:with-param>
    </xsl:call-template>#

    <br/>input 1000:
    Result:#<xsl:call-template name="StrSet">
      <xsl:with-param name="strlst">1000,</xsl:with-param>
    </xsl:call-template>#
    
    <br/>input 1000, 10 , 1000,10,10,1000: 
    Result:#<xsl:call-template name="StrSet">
      <xsl:with-param name="strlst">1000, 10 , 1000,10,10,1000 </xsl:with-param>
    </xsl:call-template>#
    --> 
    <!-- End StrSet Tests-->
  </xsl:template>

  <xsl:template name="GetAllGoogleChartMeasurings">
    <xsl:param name="populations"/>
    <xsl:param name="resultset"/>
    <xsl:param name="frameworks"/>
    
    <xsl:choose>
      <xsl:when test="contains($frameworks, ',')">
        <xsl:variable name="first" select="substring-before($frameworks, ',')"/>
        <xsl:variable name="rest">
          <xsl:call-template name="GetAllGoogleChartMeasurings">
            <xsl:with-param name="populations" select="$populations"/>
            <xsl:with-param name="resultset" select="$resultset"/>
            <xsl:with-param name="frameworks" select="substring-after($frameworks, ',')"/>
          </xsl:call-template>
        </xsl:variable>
        
        <xsl:variable name="measurings">
          <xsl:call-template name="GetFrameworkMeasurings">
            <xsl:with-param name="populations" select="$populations"/>      
            <xsl:with-param name="resultset" select="$resultset[../sysprops/prop[(@name='framework') and (@value=$first)]]"/>
          </xsl:call-template>
        </xsl:variable>
        <xsl:value-of select="concat($populations, '|', $measurings, '|', $rest)"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="measurings">
          <xsl:call-template name="GetFrameworkMeasurings">
            <xsl:with-param name="populations" select="$populations"/>      
            <xsl:with-param name="resultset" select="$resultset[../sysprops/prop[(@name='framework') and (@value=$frameworks)]]"/>
          </xsl:call-template>
        </xsl:variable>
        <xsl:value-of select="concat($populations, '|', $measurings)"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="GetFrameworkMeasurings">
    <xsl:param name="populations"/>
    <xsl:param name="resultset"/>  

    <xsl:choose>
      <xsl:when test="contains($populations, ',')">
        <xsl:variable name="first" select="substring-before($populations, ',')"/>
        <xsl:variable name="rest">
          <xsl:call-template name="GetFrameworkMeasurings">
            <xsl:with-param name="populations" select="substring-after($populations, ',')"/>
            <xsl:with-param name="resultset" select="$resultset"/>
          </xsl:call-template>
        </xsl:variable>

        <xsl:variable name="measuring">        
          <xsl:call-template name="GetAverageYValue">
            <xsl:with-param name="resultset" select="$resultset[../parameters/parameter[(@name='Population') and (@value=$first)]]"/>
          </xsl:call-template>
        </xsl:variable>
        <xsl:value-of select="concat($measuring,',',$rest)"/>
      </xsl:when>
      <xsl:otherwise>      
        <xsl:call-template name="GetAverageYValue">
          <xsl:with-param name="resultset" select="$resultset[../parameters/parameter[(@name='Population') and (@value=$populations)]]"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>        
  </xsl:template>

  <xsl:template name="StrSet">
    <xsl:param name="strlst"/>
    <xsl:variable name="result">
      <xsl:call-template name="StrLstRemDups">
        <xsl:with-param name="strlst" select="$strlst"/>
      </xsl:call-template>
    </xsl:variable>
    
    <xsl:call-template name="RemoveTrailingChar">
      <xsl:with-param name="str" select="$result"/>
      <xsl:with-param name="char" select="string(',')"/>
    </xsl:call-template>
  </xsl:template>
  
  <xsl:template name="SortStrList">
    <xsl:param name="strlst"/>
    
    <xsl:choose>
      <xsl:when test="contains($strlst, ',')">
        <xsl:variable name="first" select="substring-before($strlst, ',')"/>
        <xsl:variable name="rest">
          <xsl:call-template name="SortStrList">
            <xsl:with-param name="strlst" select="substring-after($strlst, ',')"/>
          </xsl:call-template>
        </xsl:variable>
        
        <xsl:variable name="result">
          <xsl:call-template name="InsertStrList">
            <xsl:with-param name="strlst" select="$rest"/>
            <xsl:with-param name="val" select="$first"/>
          </xsl:call-template>
        </xsl:variable>
        <xsl:value-of select="$result"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$strlst"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template> 
   
  <xsl:template name="InsertStrList">
    <xsl:param name="strlst"/>
    <xsl:param name="val"/>
    
    <xsl:choose>
      <xsl:when test="contains($strlst, ',')">
        <xsl:variable name="first" select="substring-before($strlst, ',')"/>
        <xsl:variable name="rest" select="substring-after($strlst, ',')"/>
        <xsl:choose>
          <xsl:when test="number($first) &lt; number($val)">
            <xsl:variable name="result">
              <xsl:call-template name="InsertStrList">
                <xsl:with-param name="strlst" select="$rest"/>
                <xsl:with-param name="val" select="$val"/>
              </xsl:call-template>
            </xsl:variable>
            <xsl:value-of select="concat($first, ',', $result)"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="concat($val, ',', $first, ',', $rest)"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="number($val) &lt; number($strlst)">
            <xsl:value-of select="concat($val, ',', $strlst)"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="concat($strlst, ',', $val)"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="RemoveTrailingChar">
    <xsl:param name="str"/>
    <xsl:param name="char"/>
    
    <xsl:choose>
      <xsl:when test="substring($str, string-length($str)) = $char">
        <xsl:value-of select="substring($str, 1, string-length($str) - 1)"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$str"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="StrLstRemDups">
    <xsl:param name="strlst" />
    <xsl:choose>
      <xsl:when test="contains($strlst, ',')">
        <xsl:variable name="first" select="normalize-space(substring-before($strlst, ','))"/>
        <xsl:variable name="rest">
          <xsl:call-template name="StrLstRemDups">
            <xsl:with-param name="strlst" select="substring-after($strlst, ',')"/>
          </xsl:call-template>
        </xsl:variable>
        
        <xsl:choose>
          <xsl:when test="contains(concat(',', $rest, ','), concat(',', $first, ','))">
            <xsl:value-of select="$rest"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="concat($first, ',', $rest)"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise><xsl:value-of select="normalize-space($strlst)"/></xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="StrLstLastItem">
    <xsl:param name="strlst"/>
    <xsl:choose>
      <xsl:when test="contains($strlst, ',')">
        <xsl:variable name="result">
          <xsl:call-template name="StrLstLastItem">
            <xsl:with-param name="strlst" select="substring-after($strlst, ',')"/>
          </xsl:call-template>
        </xsl:variable>
        <xsl:value-of select="$result"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$strlst"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="StrListAppendForEach">
    <xsl:param name="strlst"/>
    <xsl:param name="val"/>
    
    <xsl:choose>
      <xsl:when test="contains($strlst, ',')">
        <xsl:variable name="rest">
          <xsl:call-template name="StrListAppendForEach">
            <xsl:with-param name="strlst" select="substring-after($strlst, ',')"/>
            <xsl:with-param name="val" select="$val"/>
          </xsl:call-template>
        </xsl:variable>
        <xsl:value-of select="concat($val, $rest)"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$val"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="GetAverageYValue">
    <xsl:param name="resultset"/>
    <xsl:value-of select="round(sum($resultset/@y-value) div count($resultset))"/>
  </xsl:template>

  <xsl:template name="GetValues">
    <xsl:param name="params"/>
    <xsl:choose>
      <xsl:when test="$params">
        <xsl:variable name="first" select="$params[1]"/>
        <xsl:variable name="rest">
          <xsl:call-template name="GetValues">
            <xsl:with-param name="params" select="$params[position()!=1]"/>
          </xsl:call-template>
        </xsl:variable>
        <xsl:value-of select="concat($first/@value,',',$rest)"/>
      </xsl:when>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="GetYMeasurements">
    <xsl:param name="params"/>
    <xsl:choose>
      <xsl:when test="count($params) &gt; 1">
        <xsl:variable name="first" select="$params[1]"/>
        <xsl:variable name="rest">
          <xsl:call-template name="GetYMeasurements">
            <xsl:with-param name="params" select="$params[position()!=1]"/>
          </xsl:call-template>
        </xsl:variable>
        <xsl:value-of select="concat($first/@y-value,',',$rest)"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$params/@y-value"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
</xsl:transform>
