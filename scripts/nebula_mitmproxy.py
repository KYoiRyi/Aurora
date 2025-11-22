#!/usr/bin/env python3
"""
mitmproxy script for Nebula game server emulator
This script redirects game client requests from official servers to the local Nebula server
"""

from mitmproxy import http
import logging

# Configure logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger("nebula_proxy")

# Configuration - modify these as needed
TARGET_HOST = "localhost"  # Can be changed to another IP address
TARGET_PORT = 80  # Nebula server typically runs on port 80
TARGET_SCHEME = "http"  # Usually http for local servers
LOG_REQUESTS = True  # Set to False to reduce log spam

def request(flow: http.HTTPFlow) -> None:
    """
    Redirect requests from official game servers to local Nebula server
    """
    # Check if the request is going to official game servers
    if (".yostarplat.com" in flow.request.pretty_host or
        ".stellasora.global" in flow.request.pretty_host):
        
        # Store original info for logging
        original_host = flow.request.pretty_host
        original_url = flow.request.url
        
        # Redirect to target server
        flow.request.host = TARGET_HOST
        flow.request.scheme = TARGET_SCHEME
        
        # Set port if specified
        if TARGET_PORT:
            flow.request.port = TARGET_PORT
        elif not flow.request.port:
            flow.request.port = 80  # Default HTTP port
        
        # Log the redirection
        if LOG_REQUESTS:
            logger.info(f"Redirected: {original_host} -> {TARGET_HOST}")
            logger.debug(f"Original URL: {original_url}")
            logger.debug(f"New URL: {flow.request.url}")

def response(flow: http.HTTPFlow) -> None:
    """
    Handle responses from the server
    """
    # Log server responses for debugging
    if LOG_REQUESTS and (".yostarplat.com" in flow.request.pretty_host or
                         ".stellasora.global" in flow.request.pretty_host):
        logger.debug(f"Response status: {flow.response.status_code}")
        
        # Log potential errors
        if flow.response.status_code >= 400:
            logger.warning(f"Server returned error {flow.response.status_code} for {flow.request.url}")

def configure(updated):
    """
    Called when configuration changes
    """
    logger.info("Nebula mitmproxy script configured")

def load(loader):
    """
    Called when the addon is loaded
    """
    logger.info(f"Nebula mitmproxy script loaded - redirecting to {TARGET_HOST}")
    logger.info("Game domains will be redirected to local Nebula server")