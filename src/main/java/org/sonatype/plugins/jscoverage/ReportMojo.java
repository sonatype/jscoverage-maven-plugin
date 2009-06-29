package org.sonatype.plugins.jscoverage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.InterpolationFilterReader;

/**
 * @goal report
 * @phase post-integration-test
 * @author velo
 */
public class ReportMojo
    extends AbstractMojo
{

    /**
     * Javascript root source directory to be instrumented
     *
     * @parameter expression="${jscoverage.source}"
     * @required
     */
    private File source;

    /**
     * Javascript instrumented files destination
     *
     * @parameter expression="${jscoverage.destination}"
     */
    private File destination;

    /**
     * Coverage result in json format
     *
     * @parameter default-value="${basedir}/jscoverage.json.result"
     * @required
     */
    private File persistedResults;

    /**
     * @parameter default-value="${project.reporting.outputDirectory}/jscoverage"
     */
    private File reportOutput;

    /**
     * Reports formats, valid values: html, json, xml and txt. Default-value: [html, txt]
     *
     * @parameter
     */
    private String[] formats = { "html", "txt" };

    /**
     * @parameter
     */
    private String scripts;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( destination == null )
        {
            destination = source;
        }

        if ( !persistedResults.exists() )
        {
            getLog().warn( "Data source jscoverage.json.result doesn't exists, skipping report generation!" );
            return;
        }

        List<String> formats = Arrays.asList( this.formats );
        if ( formats.contains( "html" ) )
        {
            generateHtmlReport();
        }
        if ( formats.contains( "json" ) )
        {
            generateJsonReport();
        }
        if ( formats.contains( "txt" ) )
        {
            generateTxtReport();
        }
        if ( formats.contains( "xml" ) )
        {
            generateXmlReport();
        }
    }

    private void generateXmlReport()
        throws MojoExecutionException
    {
        InputStream input = null;
        OutputStream output = null;
        try
        {
            input = new FileInputStream( persistedResults );

            String jscoverage = IOUtil.toString( input );
            JSONObject json = JSONObject.fromObject( jscoverage );
            String xml = new XMLSerializer().write( json );
            output = new FileOutputStream( new File( reportOutput, "jscoverage.xml" ) );
            IOUtil.copy( xml, output );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Error generating XML report: " + e.getMessage(), e );
        }
        finally
        {
            IOUtil.close( input );
            IOUtil.close( output );
        }
    }

    @SuppressWarnings( "unchecked" )
    private void generateTxtReport()
        throws MojoExecutionException
    {
        InputStream input = null;
        Writer output = null;
        try
        {
            input = new FileInputStream( persistedResults );

            String jscoverage = IOUtil.toString( input );
            JSONObject json = JSONObject.fromObject( jscoverage );
            Set<String> files = json.keySet();
            Map<String, int[]> results = new LinkedHashMap<String, int[]>();
            int coveredFiles = 0;
            int totalFiles = json.size();
            int sumTotalStatements = 0;
            int sumCoveredStatements = 0;
            for ( String file : files )
            {
                JSONObject result = json.getJSONObject( file );
                JSONArray coverage = result.getJSONArray( "coverage" );
                int totalStatements = coverage.size();
                int coveredStatements = countCoveredStatements( coverage );

                sumCoveredStatements += coveredStatements;
                sumTotalStatements += totalStatements;

                if ( coveredStatements > 0 )
                {
                    coveredFiles++;
                }

                results.put( file, new int[] { coveredStatements, totalStatements } );
            }
            output = new FileWriter( new File( reportOutput, "jscoverage.txt" ) );
            output.write( "[JScoverage report, generated " + new Date() + "]\n" );
            output.write( "-------------------------------------------------------------------------------\n" );
            output.write( "OVERALL COVERAGE SUMMARY:\n" );
            output.write( "\n" );
            output.write( "[files, %]\t[statements, %]\n" );
            output.write( getPartialResult( coveredFiles, totalFiles ) + "\t"
                + getPartialResult( sumCoveredStatements, sumTotalStatements ) + "\n" );
            output.write( "\n" );
            output.write( "OVERALL STATS SUMMARY:\n" );
            output.write( "\n" );
            output.write( "total files: " + totalFiles + "\n" );
            output.write( "total statements: " + sumTotalStatements + "\n" );
            output.write( "\n" );
            output.write( "COVERAGE BREAKDOWN BY FILE:\n" );
            output.write( "\n" );
            output.write( "[statements, %]\t[name]\n" );
            for ( String file : files )
            {
                int[] cover = results.get( file );
                output.write( getPartialResult( cover[0], cover[1] ) + "\t" + file + "\n" );
            }
            output.write( "-------------------------------------------------------------------------------\n" );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Error generating TXT report: " + e.getMessage(), e );
        }
        finally
        {
            IOUtil.close( input );
            IOUtil.close( output );
        }
    }

    private String getPartialResult( int covered, int total )
    {
        double per = ( (double) covered * 100 ) / ( total );
        return Math.ceil( per ) + "% (" + covered + "/" + total + ")";
    }

    private int countCoveredStatements( JSONArray coverage )
    {
        int total = 0;
        for ( Object object : coverage )
        {
            if ( object instanceof Number )
            {
                if ( ( (Number) object ).intValue() != 0 )
                {
                    total++;
                }
            }
        }
        return total;
    }

    private void generateJsonReport()
        throws MojoExecutionException
    {
        try
        {
            FileUtils.copyFile( persistedResults, new File( reportOutput, "jscoverage.json" ) );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Error generating JSON report: " + e.getMessage(), e );
        }
    }

    private void generateHtmlReport()
        throws MojoExecutionException
    {
        try
        {
            FileUtils.copyDirectoryStructure( destination, reportOutput );
            FileUtils.copyFile( persistedResults, new File( reportOutput, "jscoverage.json" ) );
            copyUrl( "jscoverage-highlight.css" );
            copyUrl( "jscoverage-ie.css" );
            copyUrl( "jscoverage-throbber.gif" );
            copyUrl( "jscoverage.css" );
            copyUrl( "jscoverage.js" );
            copyInterpolateUrl( "jscoverage.html" );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Error generating HTML report: " + e.getMessage(), e );
        }
    }

    private void copyInterpolateUrl( String url )
        throws IOException
    {
        final Map<String, String> vars;
        if ( scripts != null )
        {
            vars = Collections.singletonMap( "scripts", scripts );
        }
        else
        {
            vars = Collections.singletonMap( "scripts", "" );
        }

        InterpolationFilterReader reader = null;
        InputStream input = null;
        FileWriter output = null;
        try
        {
            input = getClass().getResourceAsStream( "/" + url );
            reader = new InterpolationFilterReader( new InputStreamReader( input ), vars );
            output = new FileWriter( new File( reportOutput, url ) );

            IOUtil.copy( reader, output );
        }
        finally
        {
            IOUtil.close( output );
            IOUtil.close( reader );
            IOUtil.close( input );
        }
    }

    private void copyUrl( String url )
        throws IOException
    {
        FileUtils.copyURLToFile( getClass().getResource( "/" + url ), new File( reportOutput, url ) );
    }

}
