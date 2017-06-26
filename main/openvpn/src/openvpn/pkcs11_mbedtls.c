/*
 *  OpenVPN -- An application to securely tunnel IP networks
 *             over a single TCP/UDP port, with support for SSL/TLS-based
 *             session authentication and key exchange,
 *             packet encryption, packet authentication, and
 *             packet compression.
 *
 *  Copyright (C) 2002-2010 OpenVPN Technologies, Inc. <sales@openvpn.net>
 *  Copyright (C) 2010 Fox Crypto B.V. <openvpn@fox-it.com>
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License version 2
 *  as published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program (see the file COPYING included with this
 *  distribution); if not, write to the Free Software Foundation, Inc.,
 *  59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

/**
 * @file PKCS #11 mbed TLS backend
 */

#ifdef HAVE_CONFIG_H
#include "config.h"
#elif defined(_MSC_VER)
#include "config-msvc.h"
#endif

#include "syshead.h"

#if defined(ENABLE_PKCS11) && defined(ENABLE_CRYPTO_MBEDTLS)

#include "errlevel.h"
#include "pkcs11_backend.h"
#include <mbedtls/pkcs11.h>
#include <mbedtls/x509.h>

int
pkcs11_init_tls_session(pkcs11h_certificate_t certificate,
    struct tls_root_ctx * const ssl_ctx)
{
  int ret = 1;

  ASSERT (NULL != ssl_ctx);

  ALLOC_OBJ_CLEAR (ssl_ctx->crt_chain, mbedtls_x509_crt);
  if (mbedtls_pkcs11_x509_cert_bind(ssl_ctx->crt_chain, certificate)) {
      msg (M_FATAL, "PKCS#11: Cannot retrieve mbed TLS certificate object");
      goto cleanup;
  }

  ALLOC_OBJ_CLEAR (ssl_ctx->priv_key_pkcs11, mbedtls_pkcs11_context);
  if (mbedtls_pkcs11_priv_key_bind(ssl_ctx->priv_key_pkcs11, certificate)) {
      msg (M_FATAL, "PKCS#11: Cannot initialize mbed TLS private key object");
      goto cleanup;
  }

  ALLOC_OBJ_CLEAR (ssl_ctx->priv_key, mbedtls_pk_context);
  if (!mbed_ok(mbedtls_pk_setup_rsa_alt(ssl_ctx->priv_key,
      ssl_ctx->priv_key_pkcs11, mbedtls_ssl_pkcs11_decrypt,
      mbedtls_ssl_pkcs11_sign, mbedtls_ssl_pkcs11_key_len))) {
      goto cleanup;
  }

  ret = 0;

cleanup:
  return ret;
}

char *
pkcs11_certificate_dn (pkcs11h_certificate_t cert, struct gc_arena *gc)
{
  char *ret = NULL;
  char dn[1024] = {0};

  mbedtls_x509_crt mbed_crt = {0};

  if (mbedtls_pkcs11_x509_cert_bind(&mbed_crt, cert)) {
      msg (M_FATAL, "PKCS#11: Cannot retrieve mbed TLS certificate object");
      goto cleanup;
  }

  if (-1 == mbedtls_x509_dn_gets (dn, sizeof(dn), &mbed_crt.subject)) {
      msg (M_FATAL, "PKCS#11: mbed TLS cannot parse subject");
      goto cleanup;
  }

  ret = string_alloc(dn, gc);

cleanup:
  mbedtls_x509_crt_free(&mbed_crt);

  return ret;
}

int
pkcs11_certificate_serial (pkcs11h_certificate_t cert, char *serial,
    size_t serial_len)
{
  int ret = 1;

  mbedtls_x509_crt mbed_crt = {0};

  if (mbedtls_pkcs11_x509_cert_bind(&mbed_crt, cert)) {
      msg (M_FATAL, "PKCS#11: Cannot retrieve mbed TLS certificate object");
      goto cleanup;
  }

  if (-1 == mbedtls_x509_serial_gets (serial, serial_len, &mbed_crt.serial)) {
      msg (M_FATAL, "PKCS#11: mbed TLS cannot parse serial");
      goto cleanup;
  }

  ret = 0;

cleanup:
  mbedtls_x509_crt_free(&mbed_crt);

  return ret;
}
#endif /* defined(ENABLE_PKCS11) && defined(ENABLE_CRYPTO_MBEDTLS) */
