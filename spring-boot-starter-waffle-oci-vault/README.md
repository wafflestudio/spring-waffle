# spring-boot-starter-waffle-oci-vault

Loads OCI Vault Secrets into Spring `Environment` at startup (as an `EnvironmentPostProcessor`).

## Properties

Required:
- `oci.vault.secret-ids`: Comma-separated secret OCIDs (`ocid1.vaultsecret...`).
- `oci.vault.region`: Region id (default: `ap-chuncheon-1`).

Auth:
- `oci.auth.type`: `auto` (default), `instance_principal`, or `config`.
  - `config`: Uses OCI config file credentials.
  - `instance_principal`: Uses Instance Principal (Dynamic Group) credentials.
  - `auto`: Tries Instance Principal first; if it fails, falls back to config file credentials.

Config-file auth options:
- `oci.config.path`: Path to OCI config file (default: `~/.oci/config`).
- `oci.config.profile`: Profile name (default: `DEFAULT`).

## Secret Format

Each secret is expected to be a JSON object. Keys from the JSON are added as properties only if they are not already set in the environment.

## Notes

- OKE Workload Identity is an OKE feature and not implemented by this starter at the moment. If you run on OKE Basic and you cannot use instance principals from pods, use `oci.auth.type=config` (or `auto`) with a mounted `~/.oci/config` + key.
