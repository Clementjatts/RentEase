<?php
/**
 * Simple test to verify that PHPUnit assertions syntax is correct
 */

use PHPUnit\Framework\TestCase;

class SyntaxCheckTest extends TestCase
{
    /**
     * Test all the assertion types we've updated
     */
    public function testAssertionSyntax()
    {
        // Test is_array assertion
        $array = ['test' => 'value'];
        $this->assertTrue(is_array($array));
        
        // Test is_string assertion
        $string = "test string";
        $this->assertTrue(is_string($string));
        
        // Test array_key_exists assertion
        $this->assertTrue(array_key_exists('test', $array));
        
        // Test strpos assertion
        $this->assertTrue(strpos($string, 'test') !== false);
        
        // Test count assertion
        $this->assertEquals(1, count($array));
        
        // Basic equality assertion (unchanged)
        $this->assertEquals('value', $array['test']);
    }
}
