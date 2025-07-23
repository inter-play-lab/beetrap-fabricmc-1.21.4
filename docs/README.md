# Beetrap GitHub Pages

This directory contains the GitHub Pages site for the Beetrap Minecraft mod project.

## Quick Setup

**📋 For detailed configuration instructions, see [CONFIGURATION.md](CONFIGURATION.md)**

### Basic Steps:

1. **Configure the site**: Update placeholder values in `_config.yml`
2. **Enable GitHub Pages**: Go to repository Settings → Pages → Deploy from branch → main → /docs
3. **Wait for deployment**: Your site will be available at `https://your-username.github.io/beetrap-fabricmc-1.21.4/`

### Essential Configuration:

Before enabling GitHub Pages, update these values in `_config.yml`:
- `url`: Replace "your-username" with your GitHub username
- `email`: Replace with your actual email
- `github_username`: Replace with your GitHub username

## Local Development

To run the site locally for development:

1. Install Jekyll and dependencies:
   ```bash
   gem install jekyll bundler
   ```

2. Navigate to the docs directory:
   ```bash
   cd docs
   ```

3. Install dependencies (if using Gemfile):
   ```bash
   bundle install
   ```

4. Serve the site locally:
   ```bash
   jekyll serve
   ```

5. Open your browser to `http://localhost:4000`

## Customization

- Edit `index.html` to modify the main page content
- Update `_config.yml` to change site settings
- Add new pages by creating additional HTML or Markdown files
- Customize styling by modifying the CSS in `index.html` or creating separate stylesheets

## Files

- `index.html` - Main landing page with project overview
- `development.html` - Comprehensive development guide
- `_config.yml` - Jekyll configuration file
- `CONFIGURATION.md` - Detailed configuration instructions
- `README.md` - This file, documentation for the GitHub Pages setup

## Notes

- The site uses a custom HTML template with embedded CSS for simplicity
- GitHub Pages automatically builds and deploys the site when changes are pushed to the main branch
- Make sure to update the GitHub username and repository name in the configuration files
