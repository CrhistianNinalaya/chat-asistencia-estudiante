#!/usr/bin/env bash
set -euo pipefail

if [[ "${EUID}" -ne 0 ]]; then
    echo "Error: This script must be run as root. Please run with sudo." >&2
    exit 1
fi

setup_swap() {
    if [[ ! -f /swapfile ]]; then
        echo "Creating 2GB swap file at /swapfile..."
        if ! fallocate -l 2G /swapfile 2>/dev/null; then
            dd if=/dev/zero of=/swapfile bs=1M count=2048 status=progress
        fi
        chmod 600 /swapfile
        mkswap /swapfile
    fi

    if ! swapon --show | grep -q "/swapfile"; then
        echo "Enabling swap file..."
        swapon /swapfile
    fi

    if ! grep -qs "^/swapfile " /etc/fstab; then
        echo "Adding swap entry to /etc/fstab..."
        echo "/swapfile swap swap defaults 0 0" >> /etc/fstab
    fi

    echo "Tuning vm.swappiness=10..."
    echo "vm.swappiness=10" > /etc/sysctl.d/99-swap.conf
    sysctl -p /etc/sysctl.d/99-swap.conf >/dev/null 2>&1 || sysctl --system >/dev/null 2>&1 || true
}

setup_prerequisites() {
    echo "Updating system packages and installing prerequisites..."
    export DEBIAN_FRONTEND=noninteractive
    apt-get update
    apt-get install -y ca-certificates curl gnupg lsb-release nginx certbot python3-certbot-nginx
}

setup_docker() {
    echo "Configuring Docker official repository..."
    install -m 0755 -d /etc/apt/keyrings
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
    chmod a+r /etc/apt/keyrings/docker.asc

    local architecture
    architecture="$(dpkg --print-architecture)"
    local release_codename
    release_codename="$(lsb_release -cs)"

    echo "deb [arch=${architecture} signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu ${release_codename} stable" > /etc/apt/sources.list.d/docker.list

    export DEBIAN_FRONTEND=noninteractive
    apt-get update
    apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

    systemctl enable docker
    systemctl start docker

    if id -u ubuntu >/dev/null 2>&1; then
        echo "Adding user ubuntu to docker group..."
        usermod -aG docker ubuntu
    fi
}

configure_nginx() {
    systemctl enable nginx
    systemctl start nginx

    local script_dir
    script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
    local template_path="${script_dir}/nginx-reverse-proxy.conf"

    if [[ -f "${template_path}" ]]; then
        echo "Deploying Nginx reverse proxy template..."
        cp "${template_path}" /etc/nginx/sites-available/chat-support.conf
        ln -sf /etc/nginx/sites-available/chat-support.conf /etc/nginx/sites-enabled/chat-support.conf
        rm -f /etc/nginx/sites-enabled/default
        nginx -t
        systemctl reload nginx
    fi
}

setup_duckdns() {
    local domain="${1:-}"
    local token="${2:-}"

    if [[ -z "${domain}" || -z "${token}" ]]; then
        echo "Error: DuckDNS domain and token are required." >&2
        echo "Usage: $0 duckdns <domain> <token>" >&2
        return 1
    fi

    local updater_script="/usr/local/bin/update-duckdns.sh"
    cat <<EOF > "${updater_script}"
#!/usr/bin/env bash
set -euo pipefail
curl -s "https://www.duckdns.org/update?domains=${domain}&token=${token}&ip=" > /var/log/duckdns.log 2>&1
EOF
    chmod 0755 "${updater_script}"

    local cron_file="/etc/cron.d/duckdns"
    cat <<EOF > "${cron_file}"
*/5 * * * * root ${updater_script}
EOF
    chmod 0644 "${cron_file}"

    bash "${updater_script}" || true
    echo "DuckDNS cron updater configured for domain: ${domain}.duckdns.org"
}

main() {
    if [[ "${1:-}" == "duckdns" ]]; then
        setup_duckdns "${2:-}" "${3:-}"
        exit 0
    fi

    setup_swap
    setup_prerequisites
    setup_docker
    configure_nginx

    if [[ -n "${DUCKDNS_DOMAIN:-}" && -n "${DUCKDNS_TOKEN:-}" ]]; then
        setup_duckdns "${DUCKDNS_DOMAIN}" "${DUCKDNS_TOKEN}"
    else
        echo "Notice: DUCKDNS_DOMAIN and DUCKDNS_TOKEN environment variables not set."
        echo "To configure DuckDNS updater cron later, run: $0 duckdns <domain> <token>"
    fi

    echo "EC2 host setup completed successfully."
}

main "$@"
