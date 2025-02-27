<?php
/**
 * JWT (JSON Web Token) Utility Class
 * 
 * A simple JWT implementation for token-based authentication
 */
class JWT {
    /**
     * Encode a payload into a JWT
     * 
     * @param array $payload Data to encode in the token
     * @param string $key Secret key
     * @param string $alg Algorithm to use (default: HS256)
     * @return string JWT token
     */
    public static function encode($payload, $key, $alg = 'HS256') {
        $header = json_encode(['typ' => 'JWT', 'alg' => $alg]);
        $header = self::base64UrlEncode($header);
        
        $payload = json_encode($payload);
        $payload = self::base64UrlEncode($payload);
        
        $signature = hash_hmac('sha256', "$header.$payload", $key, true);
        $signature = self::base64UrlEncode($signature);
        
        return "$header.$payload.$signature";
    }
    
    /**
     * Decode a JWT and return the payload
     * 
     * @param string $jwt The token
     * @param string $key Secret key
     * @param string $alg Algorithm to use (default: HS256)
     * @return array|false Decoded payload or false if invalid
     */
    public static function decode($jwt, $key, $alg = 'HS256') {
        $parts = explode('.', $jwt);
        
        if (count($parts) != 3) {
            return false;
        }
        
        list($header, $payload, $signature) = $parts;
        
        // Verify signature
        $verified = self::verify("$header.$payload", $signature, $key, $alg);
        
        if (!$verified) {
            return false;
        }
        
        // Decode payload
        $decoded = json_decode(self::base64UrlDecode($payload), true);
        
        // Check if token has expired
        if (isset($decoded['exp']) && $decoded['exp'] < time()) {
            return false;
        }
        
        return $decoded;
    }
    
    /**
     * Verify a JWT signature
     * 
     * @param string $msg The message (header.payload)
     * @param string $signature The signature to verify
     * @param string $key Secret key
     * @param string $alg Algorithm used
     * @return bool Whether signature is valid
     */
    private static function verify($msg, $signature, $key, $alg) {
        if ($alg !== 'HS256') {
            // Only HS256 is supported currently
            return false;
        }
        
        $hash = hash_hmac('sha256', $msg, $key, true);
        $expected = self::base64UrlEncode($hash);
        
        return hash_equals($expected, $signature);
    }
    
    /**
     * Base64 URL-safe encoding
     * 
     * @param string $data Data to encode
     * @return string Base64 URL-encoded data
     */
    private static function base64UrlEncode($data) {
        return rtrim(strtr(base64_encode($data), '+/', '-_'), '=');
    }
    
    /**
     * Base64 URL-safe decoding
     * 
     * @param string $data Data to decode
     * @return string Decoded data
     */
    private static function base64UrlDecode($data) {
        return base64_decode(strtr($data, '-_', '+/'));
    }
}
