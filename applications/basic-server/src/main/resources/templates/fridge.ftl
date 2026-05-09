<#import "template.ftl" as layout />

<@layout.noauthentication>
    <section>
        <div class="container">
            <h2>Fridge: ${fridge.name}</h2>

            <#if message??>
                <p class="flash success">${message}</p>
            </#if>
            <#if error??>
                <p class="flash error">${error}</p>
            </#if>

            <div class="fridge-summary">
                <p><strong>Dimensions:</strong> ${fridge.width} × ${fridge.height} × ${fridge.depth}</p>
                <p><a href="/">Back to dashboard</a></p>
            </div>

            <section>
                <h3>Contents</h3>
                <#if contents?size == 0>
                    <p>No items placed in this fridge.</p>
                <#else>
                    <table>
                        <thead>
                            <tr><th>Item</th><th>Owner</th><th>Position</th></tr>
                        </thead>
                        <tbody>
                            <#list contents as record>
                                <tr>
                                    <td>
                                        <#if itemMap[record.itemId]?has_content>
                                            ${itemMap[record.itemId].name}
                                        <#else>
                                            Unknown item
                                        </#if>
                                    </td>
                                    <td>
                                        <#if itemMap[record.itemId]?has_content && ownerMap[itemMap[record.itemId].ownerId]?has_content>
                                            ${ownerMap[itemMap[record.itemId].ownerId].name}
                                        <#else>
                                            Unknown owner
                                        </#if>
                                    </td>
                                    <td>${record.x}, ${record.y}, ${record.z}</td>
                                </tr>
                            </#list>
                        </tbody>
                    </table>
                </#if>
            </section>

            <section>
                <h3>Place item</h3>
                <form method="post" action="/fridges/${fridge.id}/place-item" class="form-panel">
                    <label>Item ID <input type="number" name="itemId" min="1" required></label>
                    <label>X <input type="number" name="x" min="0" max="${fridge.width - 1}" required></label>
                    <label>Y <input type="number" name="y" min="0" max="${fridge.height - 1}" required></label>
                    <label>Z <input type="number" name="z" min="0" max="${fridge.depth - 1}" required></label>
                    <button type="submit">Place item</button>
                </form>
            </section>
        </div>
    </section>
</@layout.noauthentication>
